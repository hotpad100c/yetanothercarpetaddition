package mypals.ml.settings;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.Validator;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

public class RuleValidators {

    public static class MOVING_PISTON_SPEED_VALIDATOR extends Validator<Float> {

        @Override
        public Float validate(@Nullable ServerCommandSource source, CarpetRule<Float> changingRule, Float newValue, String userInput) {
            return Math.max(0.0f, Math.min(newValue, 1.0f));
        }
    }
}
