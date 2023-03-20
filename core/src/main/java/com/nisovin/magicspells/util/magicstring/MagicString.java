package com.nisovin.magicspells.util.magicstring;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.exception.MagicException;

public class MagicString {

    private static final char PLACEHOLDER_HEAD = '<';
    private static final char PLACEHOLDER_TAIL = '>';
    private static final char PLACEHOLDER_ARG_SEPARATOR = ':';

    private MagicString() {}

    public static String parse(String string, LivingEntity caster, LivingEntity target, String[] spellArgs) {
        if (string == null || string.isEmpty()) return string;
        StringBuilder newString = new StringBuilder();
        StringBuilder processingTail = new StringBuilder();
        StringBuilder placeholderName = new StringBuilder();
        List<String> placeholderArgs = new ArrayList<>();
        StringBuilder latestArg = new StringBuilder();

        boolean isProcessing = false;
        boolean isIdentified = false;
        int innerArgs = 0;
        for (char c : string.toCharArray()) {
            // Before processing: Add all text to the new string until "head" is found.
            if (!isProcessing) {
                if (c == PLACEHOLDER_HEAD) {
                    // Start processing.
                    isProcessing = true;
                    processingTail.append(c);
                    continue;
                }
                // Collect non-placeholder chars.
                newString.append(c);
                continue;
            }
            // Collect what might be a complete placeholder.
            processingTail.append(c);

            // While processing:
            // Track inner args so that we know what the last main tail is.
            if (c == PLACEHOLDER_HEAD) {
                innerArgs++;
                latestArg.append(c);
                continue;
            }
            if (c == PLACEHOLDER_TAIL) {
                // Track closed inner args.
                if (innerArgs > 0) {
                    innerArgs--;
                    latestArg.append(c);
                    // Parse inner args.
                    String oldArg = latestArg.toString();
                    String result = parse(oldArg, caster, target, spellArgs);
                    latestArg = new StringBuilder(result);
                    String oldTail = processingTail.toString();
                    processingTail = new StringBuilder(oldTail.replace(oldArg, result));
                    continue;
                }
                // End processing and attach processed placeholder.
                placeholderArgs.add(latestArg.toString());
                latestArg.setLength(0);
                MagicSpells.debug(3, "Processing MS placeholder: '" + processingTail + "'");
                try {
                    PlaceholderFunction placeholder = Placeholders.valueOf(placeholderName.toString().toUpperCase()).getInstance();
                    if (placeholder == null) newString.append(processingTail);
                    else {
                        String result = placeholder.apply(placeholderArgs, caster, target, spellArgs);
                        newString.append(result);
                    }
                }
                catch (IllegalArgumentException ignored) {
                    // This possibly wasn't an MS's placeholder.
                    newString.append(processingTail);
                }
                catch (MagicException exception) {
                    MagicSpells.error("Placeholder '" + processingTail + "' threw exception: " + exception.getMessage());
                }

                isProcessing = false;
                isIdentified = false;
                processingTail.setLength(0);
                placeholderName.setLength(0);
                placeholderArgs.clear();
                continue;
            }
            // If inner, treat everything as the arg.
            if (innerArgs > 0) {
                latestArg.append(c);
                continue;
            }
            // Shift to building the next arg.
            if (c == PLACEHOLDER_ARG_SEPARATOR) {
                if (isIdentified) {
                    placeholderArgs.add(latestArg.toString());
                    latestArg.setLength(0);
                }
                else isIdentified = true;
                continue;
            }
            if (!isIdentified) {
                placeholderName.append(c);
                continue;
            }
            latestArg.append(c);
        }

        // Attach back leftovers.
        if (!processingTail.isEmpty()) {
            newString.append(processingTail);
        }

        return newString.toString();
    }

}
