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
 * Веб-ссылка {@code http(s)://} не длиннее {@link #maxLength()} (см. {@link VideoUrls#isWebUrl}).
 * Пусто или {@code null} — ссылки нет.
 */
@Documented
@Constraint(validatedBy = WebUrl.Validator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebUrl {

    String message() default "ссылка должна начинаться с http:// или https:// и быть не длиннее {maxLength} символов";

    int maxLength() default 512;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<WebUrl, String> {
        private int maxLength;

        @Override
        public void initialize(WebUrl annotation) {
            this.maxLength = annotation.maxLength();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return VideoUrls.isWebUrl(value, maxLength);
        }
    }
}
