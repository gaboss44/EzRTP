package com.skyblockexp.ezrtp.tools;

import com.skyblockexp.ezrtp.util.MessageUtil;
import java.lang.reflect.Method;

public class GuiTitleInspector {
    public static void main(String[] args) {
        String titleRaw = "<gradient:#00b4db:#0083b0><bold>Random Teleport</bold></gradient>";
        System.out.println("raw: " + titleRaw);
        try {
            Method parse = MessageUtil.class.getMethod("parseMiniMessage", String.class);
            Object comp = parse.invoke(null, titleRaw);
            System.out.println("component.toString(): " + String.valueOf(comp));
            Method compToLegacy = MessageUtil.class.getMethod("componentToLegacy", Object.class);
            // Note: componentToLegacy expects net.kyori Component; we call via reflection and accept Object
            try {
                String legacy = (String) MessageUtil.class.getMethod("componentToLegacy", Class.forName("net.kyori.adventure.text.Component")).invoke(null, comp);
                System.out.println("componentToLegacy: " + legacy);
            } catch (Throwable ex) {
                try {
                    // fallback: attempt to call via the Object signature if present
                    String legacy = (String) compToLegacy.invoke(null, comp);
                    System.out.println("componentToLegacy (obj): " + legacy);
                } catch (Throwable ex2) {
                    System.out.println("componentToLegacy threw: " + ex2);
                }
            }
            Method serializeToMini = MessageUtil.class.getMethod("serializeToMiniMessage", Class.forName("net.kyori.adventure.text.Component"));
            String mm = (String) serializeToMini.invoke(null, comp);
            System.out.println("serializeToMiniMessage: " + mm);
            Object reparsed = parse.invoke(null, mm);
            try {
                String alt = (String) MessageUtil.class.getMethod("componentToLegacy", Class.forName("net.kyori.adventure.text.Component")).invoke(null, reparsed);
                System.out.println("componentToLegacy(parse(mm)): " + alt);
            } catch (Throwable ex3) {
                System.out.println("componentToLegacy(parse(mm)) threw: " + ex3);
            }
        } catch (Throwable t) {
            System.out.println("inspection threw: " + t);
        }
        try {
            String forced = MessageUtil.miniMessageToLegacyString(titleRaw);
            System.out.println("miniMessageToLegacyString: " + forced);
        } catch (Throwable t) {
            System.out.println("miniMessageToLegacyString threw: " + t);
        }
    }
}
