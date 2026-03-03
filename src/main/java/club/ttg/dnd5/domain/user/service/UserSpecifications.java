package club.ttg.dnd5.domain.user.service;

import club.ttg.dnd5.domain.user.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications
{
    public static Specification<User> nicknameOrEmailContains(String value)
    {
        return (root, query, cb) ->
        {
            if (value == null || value.isBlank())
            {
                return cb.conjunction();
            }

            String like = "%" + value.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("nickname")), like),
                    cb.like(cb.lower(root.get("email")), like)
            );
        };
    }
}