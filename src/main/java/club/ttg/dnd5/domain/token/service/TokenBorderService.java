package club.ttg.dnd5.domain.token.service;

import club.ttg.dnd5.domain.common.model.SectionType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenBorderService
{

    private final TokenBorderRepository tokenBorderRepository;
    private final TokenBorderMapper tokenBorderMapper;

    private final TransactionTemplate transactionTemplate;
    private final ImageService imageService;

    public TokenBorderResponse createAndUpload(final MultipartFile file)
    {
        validateFile(file);

        String url = imageService.upload(SectionType.TOKEN_BORDER, file);

        TokenBorder border = new TokenBorder();
        border.setUrl(url);

        int nextOrder = tokenBorderRepository.findMaxOrder() + 1;
        border.setOrder(nextOrder);

        return tokenBorderMapper.toResponse(tokenBorderRepository.save(border));
    }

    @Transactional
    public void updateOrder(final TokenBorderReorderRequest request)
    {
        tokenBorderRepository.lockTokenBorderReorder();

        TokenBorder border = getById(request.getId());

        int currentOrder = border.getOrder();
        int maxOrder = tokenBorderRepository.findMaxOrder();
        int desiredOrder = clamp(request.getOrder(), maxOrder);

        if (desiredOrder == currentOrder)
        {
            return;
        }

        int bufferOrder = -1_000_000; // достаточно далеко
        tokenBorderRepository.moveToBuffer(border.getId(), bufferOrder);

        if (desiredOrder < currentOrder)
        {
            int to = currentOrder - 1;

            tokenBorderRepository.moveRangeToNegative(desiredOrder, to);
            tokenBorderRepository.restoreRangeShiftUp(desiredOrder, to);
        }
        else
        {
            // нужно сдвинуть вниз диапазон [currentOrder+1, desiredOrder]
            int from = currentOrder + 1;

            tokenBorderRepository.moveRangeToNegative(from, desiredOrder);
            tokenBorderRepository.restoreRangeShiftDown(from, desiredOrder);
        }

        border.setOrder(desiredOrder);
        tokenBorderRepository.save(border);
    }

    @Transactional
    public void delete(final UUID id)
    {
        TokenBorder border = getById(id);

        transactionTemplate.executeWithoutResult(status ->
        {
            tokenBorderRepository.lockTokenBorderReorder();

            TokenBorder lockedBorder = getById(id);
            int deletedOrder = lockedBorder.getOrder();

            imageService.delete(border.getUrl());
            tokenBorderRepository.shiftAfterDelete(deletedOrder);
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
