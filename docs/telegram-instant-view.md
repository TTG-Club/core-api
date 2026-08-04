# Telegram Instant View для новостей и статей

Пост в канале — одно сообщение: карточка превью с обложкой, заголовком и анонсом (Telegram берёт их
из og-разметки IV-страницы) плюс строка-ссылка «Читать на сайте». Полный текст открывается в Telegram
нативно по кнопке на карточке (Instant View), поэтому лимит 4096 символов, хвостовые сообщения и
заливка обложки отдельным фото больше не нужны. Заголовок и анонс в тексте сообщения не повторяются —
их и так показывает карточка.

## Как это устроено

1. `GET /iv/articles/{slug}` (core-api, [ArticleInstantViewController](../src/main/java/club/ttg/dnd5/domain/article/rest/controller/ArticleInstantViewController.java))
   отдаёт запись простой HTML-страницей: `<article>`, `<h1>`, `<time datetime>`, `<img class="cover">`,
   `<div class="lead">`, `<div class="content">` плюс og-разметка.
   На домене сайта путь `/iv/**` проксирует фронт (`core-app/server/routes/iv/[...].get.ts`),
   итоговый адрес — `https://new.ttg.club/iv/articles/{slug}`.
2. Робот Instant View скачивает эту страницу и по шаблону (см. ниже) собирает статью.
3. [TelegramPublisher](../src/main/java/club/ttg/dnd5/domain/article/service/TelegramPublisher.java)
   передаёт ссылку `https://t.me/iv?url=<адрес страницы>&rhash=<rhash>` в `link_preview_options` —
   карточка с обложкой и кнопкой Instant View показывается над текстом. Сам текст сообщения — одна
   строка-ссылка на статью на сайте (пустым текст быть не может: Bot API требует непустой `text`).

Почему отдельная страница, а не страница сайта: на сайте текст записи рисуется на клиенте
(`MarkupRender` обёрнут в `<ClientOnly>`), а робот Telegram JS не исполняет — в HTML статьи пусто.
Плюс шаблон IV пришлось бы цеплять к классам Nuxt вида `_text_a64ui_31`, которые меняются при каждой
сборке.

На странице сознательно нет `<link rel="canonical">` на статью сайта: Instant View считает canonical
настоящим адресом страницы и переходит по нему — в редакторе шаблонов адрес сам подменялся с
`/iv/articles/…` на `/articles/…`, и шаблон переставал применяться. От индексации служебной страницы
хватает `<meta name="robots" content="noindex, follow">`.

## Что нужно сделать руками один раз

1. Открыть <https://instantview.telegram.org>, войти под своим Telegram-аккаунтом.
2. **My Templates → добавить домен** `new.ttg.club`.
3. В поле адреса указать любую живую страницу вида
   `https://new.ttg.club/iv/articles/<slug-любой-опубликованной-новости>`.
4. Вставить шаблон (ниже) и нажать **Preview / Track changes** — справа должна отрисоваться статья.
5. Кнопка **View in Telegram** в правом верхнем углу даёт ссылку
   `https://t.me/iv?url=…&rhash=<rhash>`. Скопировать из неё `rhash`.
6. Прописать `rhash` в переменную окружения сервиса:
   - прод (Dokploy): `TELEGRAM_INSTANT_VIEW_RHASH=<rhash>`;
   - локально: та же переменная в `local.env`.

Пока `TELEGRAM_INSTANT_VIEW_RHASH` пуст, бот публикует по-старому (длинный пост несколькими
сообщениями с обложкой) — переключение обратимо и не требует правок кода.

Локально режим не включится даже с заполненным `rhash`: при `APP_URL=localhost` робот Telegram до
страницы не дойдёт, поэтому пост уходит прежним способом. Чтобы проверить сценарий целиком, нужен
публичный `APP_URL` (например туннель ngrok на локальный бэк).

Модерация Telegram нужна только для того, чтобы Instant View показывался по ОБЫЧНЫМ ссылкам на сайт
(без `rhash`). Ссылка с `rhash` работает у всех, кому её отправили, сразу после создания шаблона —
именно её и постит бот. Отправлять шаблон на проверку («Submit template») можно в любой момент,
на работу канала это не влияет.

Важно: после правки шаблона `rhash` может смениться — тогда взять свежую ссылку через
**View in Telegram** и обновить переменную окружения.

## Шаблон

```
~version: "2.1"

?path: /iv/articles/.+

body: //article
title: $body//h1[1]
subtitle: $body//div[@class="lead"]//p[1]
cover: $body//img[@class="cover"]
published_date: $body//time/@datetime

site_name: "TTG Club"
author: "TTG Club"
author_url: "https://new.ttg.club"

# Всё, что уехало в свойства статьи, убираем из тела — иначе продублируется.
@remove: $title
@remove: $cover
@remove: $body//time
@remove: $subtitle
```

Если после `@remove: $cover` пропала и сама обложка статьи — убрать эту строку (тогда картинка
останется первым блоком текста). Строку `channel: "@<канал>"` можно добавить, если нужна кнопка
подписки в шапке Instant View.

## Проверка

```bash
# 1. Страница отдаётся бэком и содержит текст статьи
curl -s https://new.ttg.club/iv/articles/<slug> | head -c 2000

# 2. Черновики, отложенные и удалённые записи недоступны (404)
curl -s -o /dev/null -w "%{http_code}\n" https://new.ttg.club/iv/articles/<черновик>
```

3. В редакторе на instantview.telegram.org — предпросмотр по адресу страницы.
4. Отправить ссылку `https://t.me/iv?url=…&rhash=…` себе в «Избранное»: должна появиться карточка
   с обложкой и кнопкой ⚡ Instant View.
5. Опубликовать тестовую новость с галочкой «Публиковать в Telegram» и проверить пост в канале.
