package club.ttg.dnd5.domain.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ссылка на ролик YouTube или ВК Видео (см. {@link VideoUrls#isAllowed}). Пусто или {@code null} — видео нет.
 */
@Documented
@Constraint(validatedBy = VideoUrl.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface VideoUrl {

    String message() default VideoUrls.ALLOWED_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<VideoUrl, String> {
        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return VideoUrls.isAllowed(value);
        }
    }
}
