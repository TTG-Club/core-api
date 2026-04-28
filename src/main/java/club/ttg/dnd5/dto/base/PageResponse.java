package club.ttg.dnd5.dto.base;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        int page,
        int pageSize,
        long total,
        boolean hasNext
)
{
    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize, long total)
    {
        long loaded = (long) page * pageSize + items.size();
        return new PageResponse<>(items, page, pageSize, total, loaded < total);
    }
}
