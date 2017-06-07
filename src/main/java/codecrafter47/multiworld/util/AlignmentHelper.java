package codecrafter47.multiworld.util;

import codecrafter47.util.chat.ChatUtil;
import com.google.common.base.Strings;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by florian on 11.06.15.
 */
public class AlignmentHelper {

    private static final int LINE_WIDTH = 320;

    public static int getLines(BaseComponent[] text) {

        return (int) Math.ceil((double) ChatUtil.getLength(text) / LINE_WIDTH);
    }

    public static BaseComponent[] left(BaseComponent[] text, String fill, int length) {

        int textLength = ChatUtil.getLength(text);
        if (textLength >= length) {
            return text;
        }

        int paddingLength = ChatUtil.getLength(ChatUtil.parseBBCode(fill));
        List<BaseComponent> build = new ArrayList<>();
        build.addAll(Arrays.asList(text));
        build.addAll(Arrays.asList(ChatUtil.parseBBCode(Strings.repeat(fill, (int) Math.floor((double) (length - textLength) / paddingLength)))));
        return build.toArray(new BaseComponent[build.size()]);
    }

    public static BaseComponent[] center(BaseComponent[] text, String padding) {

        int length = ChatUtil.getLength(text);
        if (length >= LINE_WIDTH) {
            return text;
        }

        int paddingLength = ChatUtil.getLength(ChatUtil.parseBBCode(padding));
        double paddingNecessary = LINE_WIDTH - length;
        List<BaseComponent> build = new ArrayList<>();
        if (length == 0) {
            build.addAll(Arrays.asList(ChatUtil.parseBBCode(Strings.repeat(padding, (int) Math.floor((double) LINE_WIDTH / paddingLength)))));
        } else {
            paddingNecessary -= ChatUtil.getCharWidth((int) ' ', false) * 2;
            double paddingCount = Math.floor(paddingNecessary / paddingLength);
            int beforePadding = (int) Math.floor(paddingCount / 2.0);
            int afterPadding = (int) Math.ceil(paddingCount / 2.0);
            if (beforePadding > 0) {
                if (beforePadding > 1) {
                    build.addAll(Arrays.asList(ChatUtil.parseBBCode(Strings.repeat(padding, beforePadding))));
                }
                build.add(new TextComponent(" "));
            }
            build.addAll(Arrays.asList(text));
            if (afterPadding > 0) {
                build.add(new TextComponent(" "));
                if (afterPadding > 1) {
                    build.addAll(Arrays.asList(ChatUtil.parseBBCode(Strings.repeat(padding, afterPadding))));
                }
            }
        }

        return build.toArray(new BaseComponent[build.size()]);
    }
}
