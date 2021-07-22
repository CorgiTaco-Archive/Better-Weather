package corgitaco.betterweather;

import java.lang.reflect.Field;

public class yeet {

    public static void main(String[] args) throws Exception {
        WithPrivateFinalField pf = new WithPrivateFinalField();
        System.out.println(pf);
        Field f = pf.getClass().getDeclaredField("s");
        f.setAccessible(true);
        System.out.println("f.get(pf): " + f.get(pf));
        f.set(pf, "No, you’re not!");
        System.out.println(pf);
        System.out.println("f.get(pf): " + f.get(pf));
    }

    private static class WithPrivateFinalField {
        private final String s;

        public WithPrivateFinalField() {
            this.s = "I’m totally safe";
        }
        public String toString() {
            return "s = " + s;
        }
    }
}
