package club.ttg.dnd5.domain.bastion.player.event;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/** Кости для событий бастиона: бросает сервер, чтобы результат нельзя было подобрать. */
@Component
public class BastionDice {
    /** Бросок одной кости: от 1 до {@code sides}. */
    public int roll(int sides) {
        return ThreadLocalRandom.current().nextInt(1, sides + 1);
    }

    /** Броски нескольких одинаковых костей. */
    public List<Integer> rolls(int count, int sides) {
        return IntStream.range(0, count).mapToObj(index -> roll(sides)).toList();
    }

    /** Случайный элемент списка; список не пустой. */
    public <T> T pick(List<T> items) {
        return items.get(roll(items.size()) - 1);
    }
}
