package club.ttg.dnd5.domain.token.service;

import club.ttg.dnd5.domain.image.service.ImageService;
import club.ttg.dnd5.domain.token.model.TokenBorder;
import club.ttg.dnd5.domain.token.repository.TokenBorderRepository;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderReorderRequest;
import club.ttg.dnd5.domain.token.rest.dto.TokenBorderResponse;
import club.ttg.dnd5.domain.token.rest.mapper.TokenBorderMapper;
import club.ttg.dnd5.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenBorderService
{
    private static final String KEY_PREFIX = "token-borders/";

    private final TokenBorderRepository tokenBorderRepository;
    private final TokenBorderMapper tokenBorderMapper;

    private final TransactionTemplate transactionTemplate;
    private final ImageService imageService;

    public TokenBorderResponse createAndUpload(final MultipartFile file)
    {
        validateFile(file);

        String url = imageService.upload(KEY_PREFIX, file);

        TokenBorder border = new TokenBorder();
        border.setUrl(url);

        int nextOrder = tokenBorderRepository.findMaxOrder() + 1;
        border.setOrder(nextOrder);

        return tokenBorderMapper.toResponse(tokenBorderRepository.save(border));
    }

    public void updateOrder(final TokenBorderReorderRequest request)
    {
        TokenBorder border = getById(request.getId());

        int currentOrder = border.getOrder();
        int maxOrder = tokenBorderRepository.findMaxOrder();

        int desiredOrder = clamp(request.getOrder(), maxOrder);

        if (desiredOrder == currentOrder)
        {
            return;
        }

        if (desiredOrder < currentOrder)
        {
            tokenBorderRepository.shiftUp(desiredOrder, currentOrder - 1);
        }
        else
        {
            tokenBorderRepository.shiftDown(currentOrder + 1, desiredOrder);
        }

        border.setOrder(desiredOrder);
        tokenBorderRepository.save(border);
    }

    public void delete(final UUID id)
    {
        TokenBorder border = getById(id);
        imageService.delete(border.getUrl());

        transactionTemplate.executeWithoutResult(status ->
        {
            int deletedOrder = border.getOrder();
            tokenBorderRepository.delete(border);
            tokenBorderRepository.decrementOrdersAfter(deletedOrder);
        });
    }

    private TokenBorder getById(final UUID id)
    {
        return tokenBorderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Рамка не найдена: " + id));
    }

    private int clamp(final int value, final int max)
    {
        if (value < 1)
        {
            return 1;
        }
        return Math.min(value, max);
    }

    private void validateFile(final MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            throw new IllegalArgumentException("Требуется файл");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
        {
            throw new IllegalArgumentException("Загружены могут быть только изображения");
        }
    }

    public Collection<TokenBorderResponse> findAll()
    {
        return tokenBorderRepository.findAll(Sort.by(Sort.Direction.ASC, "order"))
                .stream()
                .map(tokenBorderMapper::toResponse)
                .toList();
    }
}
