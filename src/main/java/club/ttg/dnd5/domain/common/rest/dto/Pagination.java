package club.ttg.dnd5.domain.common.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Pagination {
    @Schema(description = "Количество элементов на странице")
    private long limit;
    @Schema(description = "Текущая страница")
    private int page;
    @Schema(description = "Количество страниц")
    private int pages;
    @Schema(description = "Общее количество элементов без фильтрации и поиска")
    private long total;
    @Schema(description = "Количество элементов с фильтрацией или поиском")
    private long filtered;
    @Schema(description = "Наличие следующей страницы")
    private boolean next;
    @Schema(description = "Наличие предыдущей страницы")
    private boolean prev;

    public static Pagination of(int page, int limit, long total, long filtered) {
        int pages = (int) Math.ceil((double) filtered / limit);
        return Pagination.builder()
                .limit(limit)
                .page(page)
                .pages(pages)
                .total(total)
                .filtered(filtered)
                .next(page + 1 < pages)
                .prev(page > 0)
                .build();
    }
}
