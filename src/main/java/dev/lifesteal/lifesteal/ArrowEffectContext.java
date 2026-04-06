package dev.lifesteal.lifesteal;

public final class ArrowEffectContext {
    private static final ThreadLocal<Integer> ARROW_HIT_DEPTH = ThreadLocal.withInitial(() -> 0);

    private ArrowEffectContext() {
    }

    public static void enterArrowHit() {
        ARROW_HIT_DEPTH.set(ARROW_HIT_DEPTH.get() + 1);
    }

    public static void exitArrowHit() {
        int depth = ARROW_HIT_DEPTH.get() - 1;
        if (depth <= 0) {
            ARROW_HIT_DEPTH.remove();
            return;
        }
        ARROW_HIT_DEPTH.set(depth);
    }

    public static boolean isInsideArrowHit() {
        return ARROW_HIT_DEPTH.get() > 0;
    }
}
