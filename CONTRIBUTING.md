# Руководство для контрибьюторов / Contributor's Guide

## Благодарим за ваш вклад! / Thank you for contributing!

Мы приветствуем ваши идеи и участие в развитии проекта. Чтобы ваш вклад был принят, пожалуйста, соблюдайте следующие правила.

We welcome your ideas and contributions to this project. Please follow the rules below to ensure your contributions are accepted.

---

## 1. Contributor License Agreement (CLA)

### На русском:

Перед участием в этом проекте, ознакомьтесь с условиями Contributor License Agreement (CLA). Отправляя Pull Request, вы подтверждаете:

1. Вы ознакомились с условиями CLA и принимаете их без исключений.
2. Ваш вклад является вашей оригинальной работой, и вы подтверждаете, что обладаете правами на его публикацию. Если ваш вклад включает материалы третьих лиц, у вас есть официальное разрешение на их использование.
3. Вы предоставляете права владельцам и участникам GitHub-организации [TTG Club](https://github.com/TTG-Club), действующим в проекте, на использование, модификацию, публикацию и распространение вашего вклада под лицензией [Apache 2.0](LICENSE.md).
4. Если вы представляете компанию или организацию, которая обладает правами на ваш вклад, вы подтверждаете, что данная организация также соглашается на передачу указанных прав.
5. Этот CLA касается только вашего вклада и не затрагивает общее использование проекта.

**Важно:** Используемый инструмент **CLA Assistant** автоматически фиксирует ваше согласие при создании Pull Request.

Если вы не согласны с условиями, пожалуйста, **не отправляйте Pull Request**.

### In English:

Before contributing to this project, please review the terms of the Contributor License Agreement (CLA). By submitting a Pull Request, you confirm the following:

1. You have read and agree to the CLA terms without exceptions.
2. Your contribution is your original work, and you have the rights to submit and publish it. If your contribution includes materials from third parties, you have obtained official permission to use those materials.
3. You grant the owners and contributors of the [TTG Club](https://github.com/TTG-Club) GitHub organization, working on this project, the rights to use, modify, publish, and distribute your contribution under the [Apache 2.0 License](LICENSE.md).
4. If you are contributing on behalf of a company or organization that holds rights to the contribution, you confirm that this entity also agrees to the transfer of these rights.
5. This CLA applies only to your contribution and does not affect general use of the project.

**Note:** The **CLA Assistant** tool we use automatically records your agreement when creating a Pull Request.

If you do not agree to these terms, please **do not submit a Pull Request**.

---

## 2. Conventional Commits / Конвенция коммитов

### На русском:

Для удобства ведения истории изменений и их отслеживания, все коммиты должны соответствовать формату [Conventional Commits](https://www.conventionalcommits.org/ru/v1.0.0/). Пожалуйста, следуйте этим правилам:

- Формат заголовка коммита:
  ```markdown
  <тип>[опционально: область]: <краткое описание>
  ```

- Примеры допустимых коммитов:
  ```markdown
  feat: добавлена новая функция для обработки запросов
  fix: исправлена ошибка отображения таблицы
  docs: обновлена документация по установке
  ```

### In English:

To maintain a clean and consistent history of changes, all commits must follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) format. Please adhere to the following rules:

- Commit header format:
  ```markdown
  <type>[optional scope]: <short description>
  ```

- Examples of valid commits:
  ```markdown
  feat: add new feature for request processing
  fix: resolve table rendering bug
  docs: update installation documentation
  ```

---

## 3. Правила Pull Request / Pull Request Guidelines

### На русском:

Чтобы ваш Pull Request был принят, пожалуйста, соблюдайте следующие требования:

1. Перед отправкой PR убедитесь, что ваш код работает и протестирован.
2. Подробно опишите причину изменений и их цель в описании PR.
3. Следуйте кодстайлу и стандартам, принятым в проекте.
4. Убедитесь, что ваш код успешно прошёл проверки линтеров и тестов (если применимо).

### In English:

To ensure your Pull Request is accepted, please follow these guidelines:

1. Before submitting a PR, ensure that your code works and has been tested.
2. Provide a detailed description of why the changes are needed and what they accomplish.
3. Follow the coding style and standards adopted in the project.
4. Make sure your code passes all linters and tests (if applicable).

---

## 4. Слияние веток / Merging branches

### На русском:

- **Фичевый PR → `dev`** — squash-коммитом: одна задача, один коммит в истории.
- **Релизный PR `dev` → `main`** — **обычным merge-коммитом** («Create a merge
  commit»), не squash.

Squash в релизе кладёт в `main` тот же код под новым SHA. Общий предок веток при
этом не меняется, поэтому следующий релизный PR видит одни и те же строки как
параллельные правки и требует разрешить конфликт, которого по коду нет. Обычное
слияние оставляет в `main` те же коммиты, что и в `dev`, и конфликт не
возникает.

Если релиз всё же ушёл в `main` squash-коммитом, положение исправляется без
правки кода — слиянием, которое фиксирует релиз в истории и оставляет дерево
`dev` нетронутым:

```bash
git checkout dev && git pull
git merge -s ours origin/main -m "chore: отметить релизный main как влитый в dev"
git push origin dev
```

Кнопка «Update branch» в PR этого не сделает: защита `dev` не даёт GitHub
создать такой коммит, а обычный push его принимает.

### In English:

- **Feature PR → `dev`** — squash merge: one task, one commit in the history.
- **Release PR `dev` → `main`** — a regular **merge commit**, not a squash.

A squashed release puts the same code into `main` under a new SHA. The merge base
stays where it was, so the next release PR sees the very same lines as parallel
edits and reports a conflict that does not exist in the code. A regular merge
keeps `main` on the same commits as `dev`, and no conflict appears.

If a release did land in `main` as a squash, it is fixed without touching the
code — by a merge that records the release in the history and leaves the `dev`
tree untouched (see the commands above). The «Update branch» button will not do
it: `dev` protection forbids GitHub from creating such a commit, while a plain
push accepts it.

---

## 5. Использование CLA Assistant / Use of CLA Assistant

### На русском:

Мы используем инструмент [CLA Assistant](https://cla-assistant.io/), который автоматически фиксирует ваше согласие с условиями CLA. Это упрощает процесс отправки Pull Request и предоставляет защиту для всех участников проекта.

При создании Pull Request CLA Assistant автоматически запросит ваше согласие, сохранит его и свяжет с вашим профилем GitHub.

### In English:

We use the [CLA Assistant](https://cla-assistant.io/) tool to automatically track your agreement to the CLA terms. This simplifies the Pull Request process and provides protection for all contributors to the project.

When creating a Pull Request, CLA Assistant will automatically request and record your agreement, linking it to your GitHub profile.

---

Спасибо за участие в улучшении нашего проекта!  
Thank you for helping improve our project! 🎉
