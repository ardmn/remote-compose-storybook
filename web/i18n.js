// The page's own words in English and Russian. The API and the code stay in English, as they are in
// Android Studio; everything that explains them, story names included, is translated.
// Strings may hold markup: they are the page's own text, never user data. app.js escapes every
// translation by default; a key whose text holds markup must be rendered with tHtml() there.
(function () {
  "use strict";

  const en = {
    "loading.title": "Loading the Remote Compose player",
    "loading.progress": "{done} of {total} MB · {pct}%",
    "loading.starting": "Starting the player…",
    "loading.failed": "The player could not be loaded. Reload the page to try again.",
    "nav.search": "Find components  ( / )",
    "nav.overview": "Overview",
    "nav.coverage": "API coverage",
    "nav.empty": "No component matches “{q}”.",
    "top.theme": "Toggle dark theme",
    "top.repo": "Source on GitHub",
    "top.lang": "Русский",
    "tool.viewport": "Viewport",
    "tool.viewport.auto": "Story default",
    "tool.viewport.fill": "Fill",
    "tool.viewport.watch192": "Watch 192 round",
    "tool.viewport.watch227": "Watch 227 round",
    "stage.round": "{size} dp round",
    "tool.layout": "Layout",
    "tool.layout.centered": "Centered",
    "tool.layout.topstart": "Top start",
    "tool.theme": "Player theme",
    "tool.theme.light": "Light",
    "tool.theme.dark": "Dark",
    "tool.reset": "Reset args",
    "tool.reset.title": "Reset every control to its default",
    "tool.link": "Copy link",
    "tool.link.title": "Copy a link to this story with these args",
    "tool.download": "Download .rc",
    "tool.download.title": "Download the current document as a .rc file",
    "tab.controls": "Controls",
    "tab.code": "Code",
    "tab.docs": "Docs",
    "tab.document": "Document",
    "tab.changes": "Changes",
    "tab.host": "Host values",
    "tab.actions": "Actions",
    "plain.what": "What it is",
    "plain.use": "Good for",
    "how.title": "How it works",
    "stage.rendering": "Rendering…",
    "stage.loading": "Loading the player…",
    "stage.failed": "This story failed to compose:\n{message}",
    "stage.crashed": "The player stopped: {message}\nReload the page to continue.",
    "stage.text": "Text on the stage: {texts}",
    stats: "{bytes} bytes · {ops} ops",
    "controls.none": "This story has no args. Interact with it on the stage.",
    "controls.name": "Name",
    "controls.control": "Control",
    "controls.description": "Description",
    "code.usage": "Usage with the current args",
    "code.source": "Story source · {file}",
    "code.missing": "Source for {fn} is missing from data/sources.json.",
    "code.copy": "Copy",
    copied: "Copied",
    copyFailed: "Copy failed",
    "docs.noSignature": "No signature found in AndroidX's api/current.txt.",
    "docs.stories": "Stories",
    "docs.reference": "Reference",
    "doc.waiting": "Waiting for the first document…",
    "doc.summary":
      "The <code>.rc</code> document this story captures to right now: <b>{bytes} bytes</b>, {ops} operations. This is what a server would send to a player{centered}.",
    "doc.centered":
      " (with the stage's centering box around the story; choose Layout: Top start to see the story alone)",
    "doc.structure": "Structure (one operation per line)",
    "changes.none": "Change a control: this tab shows what that change did to the document, operation by operation.",
    "changes.intro":
      "What the last change did to the document (structural diff of the previous and the current <code>.rc</code>).",
    "host.none":
      'This story\'s document declares no named values. The stories under <a href="#/story/named-state--card">rememberNamedRemote*</a> do: a host app can change them while the document plays, without a new document from the server.',
    "host.intro":
      "A host app can set these values by name while the document plays. Change one: the stage replays the <b>same {bytes}-byte document</b> with your value, with no new document from the server. That is how live data reaches a Remote Compose screen. Changing a control resets them, because that makes a new document.",
    "host.type": "Type",
    "host.value": "Host value",
    "host.placeholder": "the document's value",
    "host.inCode": "{kind}: set by the host app in code",
    "host.reset": "Back to the document's values",
    "host.state": "same document, host values",
    "actions.none":
      "Events the player sends to its host (hostAction, haptics, …) appear here. Try a story under Interaction and tap the stage.",
    "actions.clear": "Clear",
    "cov.title": "API coverage",
    "cov.missing": "data/coverage.json is missing.",
    "cov.lede":
      "Every public building block in AndroidX's <code>api/current.txt</code> ({source}) and the stories that use it.",
    "cov.summary": "<b>{covered}</b> of <b>{total}</b> covered{pending}.",
    "cov.pendingSummary": "; <b>{n}</b> are public but cannot be demonstrated yet (reasons below)",
    "cov.pending": "cannot be shown yet",
    "cov.none": "no story yet",
    "cov.unlinked": "used only in helper code:",
    "ov.lede":
      "A live catalog of the public <b>Jetpack Remote Compose</b> creation API, the <code>@RemoteComposable</code> building blocks from <code>androidx.compose.remote</code>. Every story is composed in your browser, captured to a <code>.rc</code> document and drawn by a player, so you can see what each block does, how to call it, and how its document changes when its arguments change.",
    "ov.counts":
      '{components} components · {stories} stories · {covered}/{blocks} public building blocks covered (<a href="#/coverage">API coverage</a>).',
    "ov.what.title": "What is Remote Compose?",
    "ov.what":
      "Remote Compose lets a server describe a piece of app UI as a small document instead of shipping new app code. The app plays the document: it lays it out, draws it, animates it and reacts to taps on the device, and the server can change it at any time without an app release. This site shows every building block such a document can be made of.",
    "ov.designers.title": "Designers",
    "ov.designers":
      "Browse the components, change their controls and see what is possible: layouts, text, images, shapes, gradients, animations and interactive states. Each component explains what it is and when to use it. Start with the Recipes: whole screens built from the blocks.",
    "ov.pm.title": "Product managers",
    "ov.pm":
      'See which kinds of screens, widgets and interactions can be updated from the server without an app release. The <a href="#/coverage">API coverage</a> page lists every building block.',
    "ov.dev.title": "Developers",
    "ov.dev":
      "Every story shows the exact Kotlin to write, the public signatures, the bytes the document becomes and how they change when an argument changes.",
    "ov.read.title": "How to read a story",
    "ov.read":
      "<b>Controls</b> change the story's arguments; the document is rebuilt on every change. <b>Code</b> shows the call with the current arguments and the story's source, written against AndroidX's packages and ready to paste. <b>Docs</b> lists the public signatures from AndroidX's <code>api/current.txt</code>. <b>Document</b> is the operation list a server would send; <b>Changes</b> is what your last change did to it; <b>Host values</b> lets you play the app's part: set a named value and the same document redraws with it, no new document; <b>Actions</b> logs the events the player sends to its host.",
    "ov.use.title": "Use it in your project",
    "ov.use":
      "The catalog follows <b>androidx-main</b>: the newest public API, a step ahead of the latest release (<code>{release}</code>). Use the next release, or AndroidX snapshots, to get every block shown here.",
    "ov.useRelease":
      "The catalog shows the public API of AndroidX's release <code>{release}</code>, the version rc-players' fork is cut from: every block here is on Google Maven.",
    "ov.capture": "Capture a @RemoteComposable to bytes (Android)",
    "ov.components": "Components",
    "ov.how.title": "How this site works",
    "ov.how":
      'The stories are compiled to WebAssembly with <a href="https://github.com/yschimke/rc-players" target="_blank" rel="noopener">rc-players</a>, a Compose Multiplatform port of AndroidX\'s creation API that differs from it only by package. Each story is composed live by <code>RcEmbeddedPlayer</code> and drawn by rc-players\' Compose player. The source shown in the Code tab is rewritten to <code>androidx.compose.remote.*</code>, and CI compiles every story, exactly as shown, against AndroidX\'s own artifacts (the AndroidX version this catalog shows), so what you copy compiles.',
  };

  const ru = {
    "loading.title": "Загружаем плеер Remote Compose",
    "loading.progress": "{done} из {total} МБ · {pct}%",
    "loading.starting": "Запускаем плеер…",
    "loading.failed": "Плеер не загрузился. Обновите страницу, чтобы попробовать снова.",
    "nav.search": "Поиск компонентов  ( / )",
    "nav.overview": "Обзор",
    "nav.coverage": "Покрытие API",
    "nav.empty": "Нет компонентов по запросу «{q}».",
    "top.theme": "Тёмная тема",
    "top.repo": "Исходники на GitHub",
    "top.lang": "English",
    "tool.viewport": "Размер",
    "tool.viewport.auto": "По умолчанию",
    "tool.viewport.fill": "Во всю ширину",
    "tool.viewport.watch192": "Часы 192, круглые",
    "tool.viewport.watch227": "Часы 227, круглые",
    "stage.round": "{size} dp, круглый",
    "tool.layout": "Раскладка",
    "tool.layout.centered": "По центру",
    "tool.layout.topstart": "Сверху слева",
    "tool.theme": "Тема плеера",
    "tool.theme.light": "Светлая",
    "tool.theme.dark": "Тёмная",
    "tool.reset": "Сбросить",
    "tool.reset.title": "Вернуть все контролы к значениям по умолчанию",
    "tool.link": "Скопировать ссылку",
    "tool.link.title": "Ссылка на эту story с текущими значениями",
    "tool.download": "Скачать .rc",
    "tool.download.title": "Скачать текущий документ как файл .rc",
    "tab.controls": "Контролы",
    "tab.code": "Код",
    "tab.docs": "Документация",
    "tab.document": "Документ",
    "tab.changes": "Изменения",
    "tab.host": "Значения приложения",
    "tab.actions": "События",
    "plain.what": "Что это",
    "plain.use": "Подходит для",
    "how.title": "Как это устроено",
    "stage.rendering": "Рисуем…",
    "stage.loading": "Загружаем плеер…",
    "stage.failed": "Эта story не собралась:\n{message}",
    "stage.crashed": "Плеер остановился: {message}\nОбновите страницу.",
    "stage.text": "Текст на сцене: {texts}",
    stats: "{bytes} байт · {ops} операций",
    "controls.none": "У этой story нет параметров. Взаимодействуйте с ней на сцене.",
    "controls.name": "Имя",
    "controls.control": "Значение",
    "controls.description": "Описание",
    "code.usage": "Вызов с текущими значениями",
    "code.source": "Исходник story · {file}",
    "code.missing": "Исходника {fn} нет в data/sources.json.",
    "code.copy": "Копировать",
    copied: "Скопировано",
    copyFailed: "Не удалось скопировать",
    "docs.noSignature": "Сигнатура не найдена в api/current.txt AndroidX.",
    "docs.stories": "Stories",
    "docs.reference": "Справочник",
    "doc.waiting": "Ждём первый документ…",
    "doc.summary":
      "Документ <code>.rc</code>, в который эта story сейчас собирается: <b>{bytes} байт</b>, операций: {ops}. Именно его сервер отправил бы плееру{centered}.",
    "doc.centered":
      " (вместе с центрирующим боксом сцены; выберите «Раскладка: Сверху слева», чтобы увидеть только story)",
    "doc.structure": "Структура (по одной операции в строке)",
    "changes.none":
      "Измените контрол — здесь появится, что именно это изменение сделало с документом, операция за операцией.",
    "changes.intro":
      "Что сделало с документом последнее изменение (структурный diff предыдущего и текущего <code>.rc</code>).",
    "host.none":
      'Документ этой story не объявляет именованных значений. Их объявляют stories в разделе <a href="#/story/named-state--card">rememberNamedRemote*</a>: приложение может менять их, пока документ играет, без нового документа с сервера.',
    "host.intro":
      "Приложение может задавать эти значения по имени, пока документ играет. Поменяйте любое: сцена проиграет <b>тот же документ ({bytes} байт)</b> с вашим значением, без нового документа с сервера. Так живые данные попадают на экран Remote Compose. Изменение контрола сбрасывает их, потому что создаёт новый документ.",
    "host.type": "Тип",
    "host.value": "Значение от приложения",
    "host.placeholder": "значение из документа",
    "host.inCode": "{kind}: задаётся приложением в коде",
    "host.reset": "Вернуть значения документа",
    "host.state": "тот же документ, значения приложения",
    "actions.none":
      "Здесь появляются события, которые плеер отправляет приложению (hostAction, тактильный отклик, …). Откройте story из раздела Interaction и нажмите на сцену.",
    "actions.clear": "Очистить",
    "cov.title": "Покрытие API",
    "cov.missing": "Нет файла data/coverage.json.",
    "cov.lede":
      "Каждый публичный строительный блок из <code>api/current.txt</code> AndroidX ({source}) и stories, которые его используют.",
    "cov.summary": "Покрыто <b>{covered}</b> из <b>{total}</b>{pending}.",
    "cov.pendingSummary": "; ещё <b>{n}</b> публичны, но пока их нельзя показать (причины ниже)",
    "cov.pending": "пока нельзя показать",
    "cov.none": "story пока нет",
    "cov.unlinked": "используется только во вспомогательном коде:",
    "ov.lede":
      "Живой каталог публичного API создания UI в <b>Jetpack Remote Compose</b> — строительных блоков <code>@RemoteComposable</code> из <code>androidx.compose.remote</code>. Каждая story собирается прямо в браузере в документ <code>.rc</code> и рисуется плеером: видно, что делает блок, как его вызвать и как меняется документ при изменении параметров.",
    "ov.counts":
      'Компонентов: {components} · stories: {stories} · покрыто {covered} из {blocks} публичных блоков (<a href="#/coverage">покрытие API</a>).',
    "ov.what.title": "Что такое Remote Compose?",
    "ov.what":
      "Remote Compose позволяет серверу описать часть интерфейса приложения небольшим документом вместо нового кода приложения. Приложение проигрывает документ: раскладывает, рисует, анимирует и реагирует на нажатия прямо на устройстве, а сервер может поменять его в любой момент без релиза. На этом сайте собраны все строительные блоки, из которых такой документ состоит.",
    "ov.designers.title": "Дизайнерам",
    "ov.designers":
      "Смотрите компоненты, меняйте параметры и проверяйте, что возможно: раскладки, текст, картинки, фигуры, градиенты, анимации и интерактивные состояния. У каждого компонента написано, что это и для чего. Начните с раздела Recipes — целые экраны, собранные из блоков.",
    "ov.pm.title": "Продакт-менеджерам",
    "ov.pm":
      'Здесь видно, какие экраны, виджеты и взаимодействия можно обновлять с сервера без релиза приложения. На странице <a href="#/coverage">покрытия API</a> перечислены все строительные блоки.',
    "ov.dev.title": "Разработчикам",
    "ov.dev":
      "В каждой story — точный Kotlin-код, публичные сигнатуры, байты документа и то, как они меняются при изменении параметра.",
    "ov.read.title": "Как читать story",
    "ov.read":
      "<b>Контролы</b> меняют параметры story, документ пересобирается при каждом изменении. <b>Код</b> — вызов с текущими значениями и исходник story на пакетах AndroidX, готовый к копированию. <b>Документация</b> — публичные сигнатуры из <code>api/current.txt</code> AndroidX. <b>Документ</b> — список операций, который отправил бы сервер; <b>Изменения</b> — что с ним сделало ваше последнее изменение; <b>Значения приложения</b> — сыграйте роль приложения: задайте именованное значение, и тот же документ перерисуется с ним без нового документа; <b>События</b> — что плеер отправляет приложению.",
    "ov.use.title": "Как подключить в проект",
    "ov.use":
      "Каталог следует <b>androidx-main</b>: это самый новый публичный API, на шаг впереди последнего релиза (<code>{release}</code>). Чтобы получить все показанные блоки, используйте следующий релиз или снапшоты AndroidX.",
    "ov.useRelease":
      "Каталог показывает публичный API релиза AndroidX <code>{release}</code> — той версии, с которой снят форк rc-players: все блоки отсюда есть в Google Maven.",
    "ov.capture": "Собрать @RemoteComposable в байты (Android)",
    "ov.components": "Компоненты",
    "ov.how.title": "Как устроен сайт",
    "ov.how":
      'Stories скомпилированы в WebAssembly с помощью <a href="https://github.com/yschimke/rc-players" target="_blank" rel="noopener">rc-players</a> — порта API создания AndroidX на Compose Multiplatform, который отличается от него только пакетом. Каждую story вживую собирает <code>RcEmbeddedPlayer</code>, а рисует Compose-плеер rc-players. Исходник во вкладке «Код» переписан на <code>androidx.compose.remote.*</code>, и CI компилирует каждую story ровно в таком виде против настоящих артефактов AndroidX (той версии, которую показывает каталог) — скопированный код компилируется.',
  };

  // Category names, and each component's plain-language description, in Russian.
  const categoriesRu = {
    Recipes: "Готовые экраны",
    Layout: "Раскладка",
    Content: "Контент",
    Canvas: "Рисование",
    Modifiers: "Модификаторы",
    Interaction: "Взаимодействие",
    Animation: "Анимация",
    "State & expressions": "Состояние и выражения",
    "Wear Material 3": "Wear Material 3",
  };

  // Each story's name and description in Russian, by story id: [name, description]. API names in
  // them stay as they are in the code. The end-to-end test fails when a story has no entry here.
  const storiesRu = {
    "remote-box--content-alignment": [
      "Выравнивание содержимого",
      "Все значения RemoteAlignment и RemoteAbsoluteAlignment на одном дочернем элементе.",
    ],
    "remote-box--layering": ["Слои", "Более поздние дочерние элементы рисуются поверх более ранних."],
    "remote-box--empty": [
      "Без содержимого",
      "RemoteBox(modifier) без лямбды содержимого: прямоугольник с размером и заливкой.",
    ],
    "remote-row--arrangement": [
      "Расстановка и выравнивание",
      "Меняйте, как ряд распределяет свободное место и выравнивает элементы разной высоты.",
    ],
    "remote-row--weights": ["Веса", "weight() делит ширину ряда пропорционально."],
    "remote-column--arrangement": [
      "Расстановка и выравнивание",
      "Распределение свободной высоты и выравнивание элементов разной ширины.",
    ],
    "remote-column--weight": [
      "Вес заполняет остаток",
      "Элемент с весом занимает высоту, которую оставили соседи фиксированного размера.",
    ],
    "remote-arrangement--spaced-by": ["spacedBy", "Один и тот же отступ для ряда и для колонки."],
    "remote-spacer--between": [
      "Фиксированный или с весом",
      "Spacer фиксированной ширины или с весом, который отодвигает следующий элемент к концу.",
    ],
    "remote-collapsible--column": [
      "Приоритеты в колонке",
      "Три элемента по 80dp в колонке, высоту которой задаёте вы.",
    ],
    "remote-collapsible--row": [
      "Приоритеты в ряду",
      "Четыре элемента в ряду, ширину которого задаёте вы; у последнего есть вес.",
    ],
    "remote-fit-box--responsive": [
      "Адаптивность",
      "Меняйте размер контейнера: плеер выбирает большой, средний или маленький вариант.",
    ],
    "remote-text--basics": ["Основы", "Текст, размер, цвет, начертание и стиль."],
    "remote-text--overflow": [
      "Переполнение и выравнивание",
      "maxLines и overflow решают, чем заканчивается длинный текст; textAlign размещает его по ширине.",
    ],
    "remote-text--families": ["Семейства шрифтов", "Общие семейства, которые плеер сопоставляет со своими шрифтами."],
    "remote-text--weights": ["Начертания", "Все именованные RemoteFontWeight."],
    "remote-text--style": [
      "RemoteTextStyle",
      "Межбуквенный интервал, высота строки, оформление и фон через один RemoteTextStyle.",
    ],
    "remote-text--variation": [
      "Вариативный шрифт",
      "Оси вариативного шрифта (weight, width), которые плеер применяет, если они есть у выбранного шрифта: у Roboto Flex, шрифта по умолчанию здесь, они есть.",
    ],
    "remote-basic-text--basics": ["Основы", "RemoteBasicText с размером, цветом и стилем."],
    "remote-image--content-scale": [
      "Масштабирование",
      "Картинка 160×90 в рамке другой формы, с каждым RemoteContentScale.",
    ],
    "remote-image--url": [
      "По URL",
      "Документ несёт только URL, картинку загружает плеер. Именованную картинку может подменить приложение.",
    ],
    "remote-basic-image--basics": ["Основы", "RemoteBasicImage с масштабированием."],
    "remote-basic-icon--vector": [
      "Векторная иконка",
      "Сердце, построенное через RemoteImageVector.Builder и path { }.",
    ],
    "remote-basic-icon--fill-type": [
      "Правило заливки контура",
      "Один контур из двух квадратов, залитый с каждым RemotePathFillType.",
    ],
    "remote-canvas--shapes": ["Фигуры", "drawRect, drawRoundRect, drawCircle, drawOval, drawArc, drawLine."],
    "remote-canvas--strokes": ["Обводка", "Толщина линии, концы и соединения."],
    "remote-canvas--text": ["drawText", "Текст, нарисованный от заданной базовой линии."],
    "remote-canvas--transforms": ["Трансформации", "Блоки translate, rotate и scale вокруг опорной точки."],
    "remote-canvas--clip": [
      "clipRect & withTransform",
      "Рисование внутри прямоугольника обрезки через одну составную трансформацию.",
    ],
    "remote-canvas--image": ["drawImage", "Встроенная картинка, нарисованная в заданной позиции."],
    "remote-canvas--use-paint": ["usePaint и режимы наложения", "Текущая кисть для блока и все RemoteBlendMode."],
    "remote-canvas--animated": [
      "Рисование по времени",
      "Позиции, которые каждый кадр вычисляются из RemoteTimeVariables.",
    ],
    "remote-brush--gradients": [
      "Градиенты",
      "Линейный, горизонтальный, вертикальный, радиальный и круговой градиенты.",
    ],
    "remote-brush--image": ["Кисть из картинки", "Картинка в роли кисти, с каждым RemoteTileMode."],
    "remote-outline--draw": ["drawOutline", "Фигура со скруглёнными углами, нарисованная на канвасе."],
    "draw-with-content--ring": ["Под или поверх", "Круг, нарисованный до или после drawContent()."],
    "remote-component--size": ["Размер от родителя", "Дочерний элемент размером в долю измеренной высоты родителя."],
    "remote-component--position": [
      "Положение в родителе и в корне",
      "positionInParent и positionInRoot колонки — там, где её разместил плеер.",
    ],
    "remote-text-measurer--metrics": [
      "Границы строки, шрифта и букв",
      "measureSingleLine, measureFontMetrics и measurePaintBounds, нарисованные вокруг текста на канвасе.",
    ],
    "remote-text-measurer--badge": [
      "Бейдж по размеру текста",
      "«Таблетка» по размеру из measureSingleLine или из measureMonospaceWidth для цифр без скачков ширины.",
    ],
    "size-modifiers--fixed": ["size()", "Фиксированный размер в dp."],
    "size-modifiers--fill": ["fillMax*", "fillMaxWidth / fillMaxHeight с долей от родителя."],
    "size-modifiers--fill-parent": ["fillParentMax*", "fillParentMaxWidth / Height и fillMaxSize(fraction)."],
    "size-modifiers--constraints": [
      "widthIn / heightIn / defaultMinSize",
      "Границы для размера, который запрашивает содержимое.",
    ],
    "size-modifiers--wrap": ["wrapContent*", "Содержимое само выбирает размер внутри большего ограничения."],
    "padding-offset--padding": [
      "padding",
      "Отступы по сторонам, по горизонтали/вертикали и через RemotePaddingValues.",
    ],
    "padding-offset--offset": ["offset & zIndex", "Сдвиньте B поверх A, затем поменяйте, кто из них сверху."],
    "background-border-clip--shape": ["Формы", "Одна и та же форма обрезает фон и рисует обводку."],
    "background-border-clip--corners": ["Радиус каждого угла", "Каждый угол отдельно и RemoteCornerSize в процентах."],
    "background-border-clip--painter": ["Фон из painter", "background(painterRemoteColor(…))."],
    "background-border-clip--clip": ["clip", "Один бокс, обрезанный по каждой форме."],
    "transform-modifiers--simple": ["alpha / rotate / scale", "Три самые частые трансформации отрисовки."],
    "transform-modifiers--graphics-layer": ["graphicsLayer", "3D-повороты, масштаб и тень на слое."],
    "transform-modifiers--graphics-layer-block": ["graphicsLayer { }", "Блочная форма graphicsLayer."],
    "transform-modifiers--compositing": [
      "Стратегия композитинга",
      "Полупрозрачный слой из двух перекрывающихся боксов с каждой RemoteCompositingStrategy. При alpha ниже 1 Auto тоже рисует вне экрана, поэтому выглядит как Offscreen.",
    ],
    "visibility--states": ["Visible / invisible / gone", "Средний бокс в трёх состояниях видимости."],
    "scroll--vertical": ["Вертикальная", "Прокручиваемая колонка; при notches > 0 прокрутка доводится до позиций."],
    "scroll--horizontal": ["Горизонтальная", "Прокручиваемый ряд."],
    "align-by-baseline--row": ["Разные размеры", "Два текста на одной базовой линии."],
    "clickable--toggle": ["Переключатель", "Булево значение, которое меняется по тапу и выбирает цвет и текст."],
    "clickable--counter": ["Счётчик", "Целое число, которое по тапу меняют выражения."],
    "host-action--named": ["Именованные действия приложения", "Именованное действие со значением и без."],
    "host-action--by-id": [
      "Действие по id или никакого",
      "hostAction с числовым id и текстовыми метаданными; RemoteAction.None, когда отключено.",
    ],
    "host-action--combined": ["combinedAction", "Изменить локальное состояние и уведомить приложение одним тапом."],
    "touch--press": ["Состояние нажатия", "«Нажато», пока палец удерживается, и счётчик касаний."],
    "semantics--properties": ["Свойства", "contentDescription, stateDescription и role; см. вкладку «Документ»."],
    "animate-value--tween": ["Tween и easing", "Анимация alpha с каждым RemoteEasing."],
    "animate-value--spring": ["Пружина", "Размер, анимированный пружиной."],
    "enter-exit--transitions": ["Появление и исчезновение", "Все RemoteEnterTransition и RemoteExitTransition."],
    "enter-exit--fade": ["animationSpec + fade", "animationSpec с remoteFadeIn / remoteFadeOut."],
    "enter-exit--toggle": ["animationSpec(enabled)", "Изменения раскладки с анимацией или мгновенно."],
    "shared-element--move": ["Между состояниями", "Один и тот же key в обеих ветках."],
    "remote-state-layout--boolean": ["Boolean", "Две ветки, которые выбирает RemoteBoolean."],
    "remote-state-layout--int": ["Int", "Три ветки, которые выбирает RemoteInt."],
    "remote-state-layout--enum": ["Enum", "По ветке на каждое значение enum."],
    "named-state--card": ["Именованные значения", "Именованные значения типов String, float, цвет, boolean, dp и int."],
    "named-state--mutable": [
      "Изменяемые значения",
      "MutableRemoteFloat, String и Int, которые меняет одно combined-действие.",
    ],
    "named-state--mutable-image": [
      "Изменяемая картинка",
      "Изменяемая картинка, объявленная документом. Контрол remembered переключает rememberMutableRemoteImageBitmap и remember { MutableRemoteImageBitmap }; документ при этом тот же.",
    ],
    "named-state--enum": ["Именованный enum", "Enum, который приложение может задать по имени."],
    "named-state--domain": [
      "Домены",
      "Одно и то же именованное значение в домене user, без префикса или в собственном пространстве имён приложения.",
    ],
    "expressions--functions": ["Математические функции", "y = f(x) по 61 выражению, которые вычисляет плеер."],
    "expressions--width": ["Размер из выражения", "Ширина, вычисленная из времени."],
    "color-expressions--animated": ["lerp / hsv / copy", "Три цветовых выражения, которые зависят от времени."],
    "time-config--clock": ["Часы", "Идущие часы без новых документов."],
    "time-config--configuration": [
      "RemoteConfiguration",
      "Окно плеера, плотность и размер шрифта. Поменяйте «Размер».",
    ],
    "recipe-product-card--default": [
      "Карточка товара",
      "Нажмите кнопку: счётчик обновится на устройстве, а приложение получит add_to_cart (вкладка «События»).",
    ],
    "recipe-weather-widget--default": [
      "Виджет погоды",
      "Выберите в «Размер» Widget 320×180 или Small 240×240, чтобы раскладка переключилась; поменяйте temperature во вкладке «Значения приложения», чтобы увидеть живые данные без нового документа.",
    ],
    "recipe-onboarding--default": ["Онбординг", "Нажмите «Next»."],
    "recipe-media-player--default": ["Медиаплеер", "Нажмите play."],
    "recipe-settings--default": [
      "Экран настроек",
      "Нажмите на переключатель; задайте wifi или brightness во вкладке «Значения приложения».",
    ],
    "wear-button--variants": [
      "Варианты кнопки",
      "Все наборы цветов RemoteButtonDefaults, с иконкой и второй строкой или без. Известное ограничение: нажатие не даёт отклика. В remote-material3 нет состояния pressed, а Compose-плееры (включая этот) рисуют ripple только там, где его просит документ; View-плеер AndroidX рисует временный ripple при любом клике (b/585818680).",
    ],
    "wear-compact-button--default": [
      "Компактная кнопка",
      "Иконка и подпись в компактном размере, в каждом наборе цветов. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-edge-button--sizes": [
      "Размеры",
      "Нажмите её: счётчик выше — состояние документа, которое обновляет плеер. Story добавляет RemoteEdgeButtonDefaults.VerticalPadding под кнопкой, как EdgeButton в Wear Material 3; сам RemoteEdgeButton его не добавляет и касается края экрана. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-icon-button--variants": [
      "Варианты кнопки-иконки",
      "Залитая, тональная и с обводкой кнопки-иконки рядом. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-text-button--variants": [
      "Варианты текстовой кнопки",
      "Тональная и обычная текстовая кнопка. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-toggle-buttons--default": [
      "Иконка и текст",
      "Нажмите любую кнопку, чтобы переключить её; варианты форм меняют форму во включённом состоянии. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-button-group--default": [
      "Группа кнопок",
      "Две или три кнопки-иконки делят ряд по весу (средняя из трёх шире). Нажмите и удерживайте одну: animateWidth пружиной расширяет её и сжимает остальные, а после отпускания возвращает. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-checkbox-button--default": [
      "Чекбокс и разделённая",
      "Нажмите, чтобы переключить; подпись разделённой кнопки вместо этого отправляет действие приложению. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-switch-button--default": [
      "Переключатель и разделённая",
      "Нажмите, чтобы переключить; подпись разделённой кнопки вместо этого отправляет действие приложению. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-radio-button--default": [
      "Радиокнопка и разделённая",
      "Один выбранный индекс в документе: выбор одного варианта снимает выбор с другого. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-card--variants": [
      "Залитая",
      "RemoteCard с формой, отступами и двумя цветами, которые она рисует (контейнер и содержимое). Выключенный enabled только отключает нажатие: у карточек нет цветов для неактивного состояния. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-card--outlined": [
      "С обводкой",
      "RemoteOutlinedCard: прозрачный контейнер в обводке, цвет и толщина которой берутся из outlinedCardBorder. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-card--image": [
      "Фон из картинки",
      "Карточка с заголовком поверх картинки: RemoteCardDefaults.containerPainter рисует картинку под содержимым с затемнением, чтобы текст читался. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-app-card--default": [
      "Карточка приложения",
      "Картинка и название приложения, время, заголовок и текст в раскладке карточки Wear. Пустое время или текст убирают свой слот. Цвета — пять RemoteCardColors, которые рисует карточка приложения (подзаголовка у неё нет). Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-title-card--default": [
      "Карточка с заголовком",
      "Заголовок, время, подзаголовок и текст; пустое время, подзаголовок или текст убирают свой слот. Цвета — пять RemoteCardColors, которые рисует карточка с заголовком (названия приложения у неё нет). Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-text--default": ["Текст", "Title medium по центру, с «…» после maxLines."],
    "wear-icon--default": ["Иконка", "Векторная иконка заданного размера и цвета."],
    "wear-circular-progress--default": [
      "Круговой прогресс",
      "Определённый прогресс или бесконечный спиннер. На 100% кольцо замыкается в полный круг.",
    ],
    "wear-linear-progress--default": ["Линейный прогресс", "Полоса прогресса через весь экран."],
    "wear-curved-progress--default": [
      "Изогнутый прогресс",
      "Индикатор вдоль края: по умолчанию от 135° с охватом 90°. Известное ограничение: на 100% при охвате 360° кольцо не смыкается. Полная дуга начинается на половину толщины линии после startAngle и короче на одну толщину, поэтому её скруглённые концы сходятся в startAngle с перетяжкой. Это баг изогнутых индикаторов (этого и intro/outro), а не задуманный вид. У кругового прогресса был похожий баг — зазор на 100%, — его исправляет remote-material3 1.0.0-alpha16. Изогнутые индикаторы по-прежнему с перетяжкой, и исправления для них в AndroidX пока нет.",
    ],
    "wear-curved-progress--intro-outro": [
      "Intro и outro",
      "Нажимайте на экран, чтобы менять прогресс 0 → 50% → 100% → 0. Индикатор intro/outro сделан для таймеров и целей (шаги, тренировка): когда прогресс уходит от 0, точка в начале вырастает и толкает трек перед собой; когда прогресс доходит до 100%, хвост трека схлопывается, и дуга добегает до конца примерно за 0.85 с. Пружины играют, только когда прогресс меняется внутри документа, поэтому story держит его в MutableRemoteFloat и меняет по тапу. Известное ограничение: на 100% при охвате 360° кольцо сходится с перетяжкой (см. «Изогнутый прогресс»). Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
    "wear-page-indicator--default": [
      "Индикатор страниц",
      "Горизонтальные или вертикальные точки для числа страниц и выбранной страницы.",
    ],
    "wear-theme--colors": [
      "Цвета, формы и анимация",
      "Копия цветовой схемы по умолчанию с заменёнными основными ролями, набор форм и схема анимации на тексте, карточке с заголовком, полосе прогресса, предупреждении и кнопке. Схема анимации задаёт пружины анимированных компонентов (например, нажатие в группе кнопок), поэтому на неподвижном экране изменений не видно. Известное ограничение: нажатие не даёт отклика (в remote-material3 нет состояния pressed; см. Button).",
    ],
  };

  const plainRu = {
    "remote-box": [
      "Контейнер, который кладёт элементы друг на друга, как слои в графическом редакторе, и прижимает их к углу, краю или центру.",
      ["Бейдж поверх аватара", "Текст по центру цветной карточки", "Фон под содержимым"],
    ],
    "remote-row": [
      "Ставит элементы в ряд слева направо и решает, как поделить между ними свободное место по горизонтали.",
      ["Иконка рядом с подписью", "Панель кнопок", "Колонки в заданной пропорции"],
    ],
    "remote-column": [
      "Ставит элементы сверху вниз и решает, как поделить между ними свободное место по вертикали.",
      ["Карточка с заголовком, текстом и подвалом", "Список настроек", "Шапка и подвал, а между ними — содержимое"],
    ],
    "remote-arrangement": [
      "Одинаковый отступ между всеми элементами ряда или колонки, как в auto-layout в Figma.",
      ["Ровные отступы в списках и панелях"],
    ],
    "remote-spacer": [
      "Невидимый промежуток: фиксированный задаёт расстояние, гибкий отодвигает следующий элемент к краю.",
      ["Прижать кнопку к правому краю", "Добавить воздуха между блоками"],
    ],
    "remote-collapsible": [
      "Ряд или колонка, которые прячут наименее важные элементы, когда места не хватает, а не сжимают и не обрезают их.",
      [
        "Виджеты, которые работают в нескольких размерах",
        "Панели, которые на маленьком экране убирают второстепенные действия",
      ],
    ],
    "remote-fit-box": [
      "Держит несколько вариантов одного содержимого, от большого к маленькому, и показывает самый большой, который помещается. Решает устройство, а не сервер.",
      [
        "Виджет с большой, средней и маленькой раскладкой",
        "Адаптивные карточки без отдельных макетов под каждое устройство",
      ],
    ],
    "remote-text": [
      "Показывает текст с нужным размером, цветом, начертанием, шрифтом, выравниванием и ограничением строк; если не помещается — заканчивается «…».",
      ["Заголовки, подписи, абзацы", "Цены и статусы, которые обновляет сервер"],
    ],
    "remote-basic-text": [
      "Самый простой текст без встроенного оформления — основа, из которой строится более богатый текст.",
      ["Полностью свои стили текста"],
    ],
    "remote-image": [
      "Показывает картинку — встроенную в экран или по веб-адресу — с обрезкой или вписыванием в рамку.",
      ["Фото товаров и баннеры", "Аватары и превью", "Картинки, которые сервер меняет без обновления приложения"],
    ],
    "remote-basic-image": [
      "Самый простой элемент-картинка без дополнительного поведения.",
      ["Декоративные изображения"],
    ],
    "remote-basic-icon": [
      "Небольшая векторная иконка: остаётся чёткой в любом размере и может менять цвет.",
      ["Иконки действий", "Значки статуса, меняющие цвет"],
    ],
    "remote-canvas": [
      "Свободная поверхность для рисования: фигуры, линии, текст и трансформации ровно там, где вы их разместили, включая анимацию.",
      ["Графики, шкалы и кольца прогресса", "Свои иллюстрации", "Анимированная графика в духе циферблатов"],
    ],
    "remote-brush": [
      "Цветовые градиенты — линейные, радиальные и круговые — для заливки фигур.",
      ["Градиентные фоны и кнопки", "Яркие индикаторы прогресса"],
    ],
    "remote-outline": [
      "Превращает фигуру, например скруглённый прямоугольник, в контур, который можно залить или использовать как маску при рисовании.",
      ["Свои карточки и рамки на canvas"],
    ],
    "draw-with-content": [
      "Дорисовывает украшения под или поверх любого компонента, не меняя сам компонент.",
      ["Подсветка и свечение", "Наложения: бейджи, затемнение"],
    ],
    "remote-component": [
      "Даёт элементу узнать реальный размер родителя и своё положение на устройстве, чтобы занять долю родителя или подстроиться под то, где он оказался.",
      ["Полосы и заливки в процентах от контейнера", "Украшения, выровненные по месту элемента"],
    ],
    "remote-text-measurer": [
      "Измеряет текст на устройстве его собственными шрифтами, чтобы рисунок подстроился под текст: ширину и высоту строки, положение базовой линии и пиксели, которые занимают буквы.",
      [
        "Бейджи и «таблетки» по размеру подписи",
        "Подписи и подчёркивания на графиках",
        "Счётчики, которые не меняют ширину, когда меняются цифры",
      ],
    ],
    "size-modifiers": [
      "Задаёт размер: точный, долю доступного места, минимум или максимум, или ровно по содержимому.",
      ["Кнопки фиксированного размера", "Баннеры во всю ширину", "Текстовые блоки с максимальной шириной"],
    ],
    "padding-offset": [
      "Внутренние отступы, сдвиг элемента с его места и выбор, какой из двух перекрывающихся элементов сверху.",
      ["Удобные области нажатия", "Перекрывающиеся аватары", "Выбранная карточка поверх остальных"],
    ],
    "background-border-clip": [
      "Заливает элемент цветом или картинкой, рисует обводку и скругляет или меняет форму углов.",
      ["Карточки и чипы", "Круглые аватары", "Кнопки-«таблетки»"],
    ],
    "transform-modifiers": [
      "Делает элемент прозрачнее, поворачивает, масштабирует, наклоняет в 3D или добавляет тень — не сдвигая ничего вокруг.",
      ["Неактивные состояния (полупрозрачные)", "Наклонённые и переворачивающиеся карточки", "Акцент через увеличение"],
    ],
    visibility: [
      "Показывает или прячет элемент — с сохранением места или без. Устройство переключает это само, без нового экрана с сервера.",
      ["Раскрывающиеся секции", "Необязательные бейджи"],
    ],
    scroll: [
      "Прокрутка содержимого, которое длиннее или шире рамки, с доводкой до позиций, целиком на устройстве.",
      ["Списки и ленты", "Горизонтальные карусели"],
    ],
    "align-by-baseline": [
      "Выравнивает тексты разного размера по одной базовой линии, как это делают типографы.",
      ["Крупная цена рядом с мелким значком валюты"],
    ],
    clickable: [
      "Делает элемент нажимаемым и сразу меняет то, что показано на устройстве, — переключатели, счётчики, выбор — без запроса к серверу.",
      ["Переключатели и чекбоксы", "Счётчики", "Вкладки и выбор"],
    ],
    "host-action": [
      "Нажатие, которое сообщает приложению вокруг экрана, что нужно сделать: открыть страницу или сохранить закладку, — заодно обновляя экран.",
      ["Переходы на экраны приложения", "Кнопки «нравится», «в закладки», «поделиться»"],
    ],
    touch: [
      "Реагирует на момент касания, отпускания или ухода пальца — для отклика на нажатие и удержание.",
      ["Состояние «нажато»", "Кнопки «удерживайте для подтверждения»"],
    ],
    semantics: [
      "Описывает элементы для скринридеров и других вспомогательных технологий: что это, в каком состоянии и что делает.",
      ["Доступность каждого экрана", "Подписи для кнопок-иконок"],
    ],
    "animate-value": [
      "Плавно анимирует значение — размер или прозрачность — при изменении, с нужной скоростью и характером (плавно, с отскоком, пружиной).",
      ["Раскрывающиеся карточки", "Плавное появление и исчезновение", "Игривый отклик"],
    ],
    "enter-exit": [
      "Анимации появления и исчезновения элемента: проявление, выезд от края, поворот или рассыпание на частицы.",
      ["Показ и скрытие панелей", "Всплывающие уведомления и баннеры"],
    ],
    "shared-element": [
      "Плавно переносит один и тот же элемент из одной раскладки в другую, чтобы пользователь видел, куда он делся.",
      ["Превью, которое разворачивается в детальный экран", "Раскрытие компактной карточки"],
    ],
    "remote-state-layout": [
      "Держит несколько версий экрана — например, загрузка, содержимое и ошибка — и показывает нужную для текущего состояния, переключаясь на устройстве.",
      ["Состояния загрузки, успеха и ошибки", "Пошаговые сценарии", "Свёрнутый и развёрнутый вид"],
    ],
    "named-state": [
      "Значения с именем, например заголовок или цвет, которые приложение может поменять позже без нового дизайна с сервера.",
      ["Живые счета и цены", "Персональные приветствия", "Цвета темы, выбранные приложением"],
    ],
    expressions: [
      "Вычисления на устройстве, хоть каждый кадр: элементы двигаются и меняют размер по формуле или по времени без новых данных.",
      ["Анимированные графики и волны", "Размеры, вычисленные из других размеров"],
    ],
    "color-expressions": [
      "Цвета, вычисляемые на устройстве: смешивание двух цветов, вращение оттенка, изменение прозрачности.",
      ["Плавно меняющиеся цвета статуса", "Анимированные фоны"],
    ],
    "time-config": [
      "То, что знает только устройство: текущее время и дата, размер экрана, плотность и размер шрифта.",
      ["Часы и обратный отсчёт", "Раскладки под размер экрана", "Учёт размера шрифта пользователя"],
    ],
    "recipe-product-card": [
      "Готовая карточка товара из блоков этого сайта, с работающей кнопкой корзины.",
      ["Промо в магазине, которое сервер меняет каждый день", "Персональные предложения"],
    ],
    "recipe-weather-widget": [
      "Виджет, который перестраивается под свой размер и показывает живые данные от приложения.",
      ["Виджеты на главном экране", "Живые счета, цены, статус доставки"],
    ],
    "recipe-onboarding": [
      "Пошаговый сценарий: все шаги приходят сразу и работают на устройстве.",
      ["Онбординг и обучение", "Опросы и пошаговые формы"],
    ],
    "recipe-media-player": [
      "Кнопки воспроизведения с движущейся полосой прогресса — всё анимируется на устройстве.",
      ["Управление музыкой и подкастами", "Таймеры и индикаторы прогресса"],
    ],
    "recipe-settings": [
      "Список переключателей, которые пользователь щёлкает на устройстве, и значения, которые держит в актуальном виде приложение.",
      ["Настройки", "Фича-флаги на удалённом экране"],
    ],
    "wear-button": [
      "Основная кнопка на часах Wear OS: широкая «таблетка» с подписью, иконкой и второй строкой, в пяти уровнях акцента.",
      ["Начать тренировку", "Ответить на сообщение", "Подтвердить действие"],
    ],
    "wear-compact-button": [
      "Короткая низкая кнопка для второстепенных действий, которым не нужна целая строка.",
      ["Второстепенные действия под карточкой", "Лайк или «поделиться»"],
    ],
    "wear-edge-button": [
      "Кнопка, которая прилегает к нижнему краю круглого экрана, — для главного действия экрана.",
      ["Главное действие экрана", "Готово, Старт или Открыть на телефоне"],
    ],
    "wear-icon-button": [
      "Круглая кнопка только с иконкой — для действий, понятных с первого взгляда.",
      ["Воспроизведение, пауза, звонок", "Ряды быстрых действий"],
    ],
    "wear-text-button": [
      "Круглая кнопка с парой символов текста вместо иконки.",
      ["Цифровые клавиатуры", "Короткие варианты вроде 5, 10, 15 мин"],
    ],
    "wear-toggle-buttons": [
      "Круглые кнопки, которые остаются включёнными или выключенными, с иконкой или коротким текстом.",
      ["Без звука, фонарик, не беспокоить", "Варианты форматирования"],
    ],
    "wear-button-group": [
      "Ряд кнопок, которые поровну делят ширину экрана.",
      ["Управление медиа", "Два-три связанных быстрых действия"],
    ],
    "wear-checkbox-button": [
      "Широкая кнопка с чекбоксом; в разделённой версии подпись открывает подробности, а чекбокс переключается отдельно.",
      ["Выбор нескольких пунктов", "Настройки с экраном подробностей"],
    ],
    "wear-switch-button": [
      "Широкая кнопка с переключателем вкл/выкл, обычная или разделённая.",
      ["Wi-Fi, Bluetooth, уведомления", "Включение и выключение функций"],
    ],
    "wear-radio-button": [
      "Широкие кнопки для выбора ровно одного варианта из списка.",
      ["Единицы и форматы", "Выбор одного из нескольких тарифов"],
    ],
    "wear-card": [
      "Нажимаемая поверхность, которая группирует кусок контента: залитая, с обводкой или поверх картинки.",
      ["Напоминание или следующее событие", "Сводка, открывающая подробности"],
    ],
    "wear-app-card": [
      "Карточка в стиле уведомления: название приложения, время, заголовок и текст.",
      ["Сообщения и уведомления", "Обновления от приложения"],
    ],
    "wear-title-card": [
      "Карточка с крупным заголовком, временем и подзаголовком.",
      ["Рейсы, доставки, встречи", "Всё, что узнаётся по заголовку"],
    ],
    "wear-text": [
      "Текст в шрифтовой шкале и цветах часов, чтобы он совпадал с остальной системой.",
      ["Заголовки и подписи на экране часов", "Короткие статусы"],
    ],
    "wear-icon": ["Векторная иконка, окрашенная в цвета темы.", ["Иконки в кнопках и карточках", "Значки статуса"]],
    "wear-circular-progress": [
      "Кольцо, которое заполняется по мере прогресса или крутится, пока объём неизвестен.",
      ["Загрузка и синхронизация", "Цели вроде шагов или минут"],
    ],
    "wear-linear-progress": [
      "Горизонтальная полоса, которая заполняется по мере прогресса.",
      ["Прогресс внутри карточки", "Заряд батареи или занятое место"],
    ],
    "wear-curved-progress": [
      "Прогресс вдоль края круглого экрана, вокруг остального содержимого.",
      ["Прогресс тренировки или таймера", "Полноэкранные обратные отсчёты"],
    ],
    "wear-page-indicator": [
      "Точки, показывающие, какая страница из пролистываемого набора сейчас на экране.",
      ["Страницы онбординга", "Перелистывание плиток или карточек"],
    ],
    "wear-theme": [
      "Дизайн-токены часов: цвета, шрифты, формы и анимации, которые читает каждый компонент, — один цвет бренда меняет их все.",
      ["Цвета бренда на удалённом экране часов", "Акцентный цвет пользователя"],
    ],
  };

  // "How it works": what the API does under the hood, for components where that is not obvious.
  // Checked against AndroidX's sources (remote-creation-compose RemoteStateLayout.kt, RemoteEnum.kt).
  const howEn = {
    "remote-state-layout": `
      <p>The state type exists only in your Kotlin, while the document is captured. The device gets an
      integer and every variant of the screen at once.</p>
      <ol>
        <li><code>MutableRemoteEnum(Screen.Loading)</code> is a <code>MutableRemoteInt</code> holding the
        <code>ordinal</code>; the enum entries stay in Kotlin. The player never sees <code>Screen</code> or its names.</li>
        <li><code>RemoteStateLayout</code> calls your lambda <b>once per state</b> at capture time. Each call becomes
        one branch of a <code>StateLayout</code> operation that points at that integer, with its colours, texts and
        your <code>when</code> already resolved to constants (see the Document tab).</li>
        <li><code>valueChangeAction(screen, RemoteEnum(next))</code> is written as “set the integer to N”.</li>
        <li>On the device the player shows the branch for the current value; a tap changes the value and the
        branch switches at once, offline, with no server and no Kotlin.</li>
      </ol>
      <p><b>So:</b> switching is instant, but every state adds a branch to the document; logic in the lambda runs
      once on the server, not on each tap. With <code>rememberNamedRemoteEnum("screen", …)</code> the integer gets a
      name (<code>USER:screen</code>) and the host app can switch the screen too (Host values tab).</p>`,
  };

  const howRu = {
    "remote-state-layout": `
      <p>Тип состояния существует только в вашем Kotlin, пока собирается документ. На устройство приходят целое
      число и сразу все варианты экрана.</p>
      <ol>
        <li><code>MutableRemoteEnum(Screen.Loading)</code> — это <code>MutableRemoteInt</code> с <code>ordinal</code>;
        список значений enum остаётся в Kotlin. Плеер не видит ни <code>Screen</code>, ни имён его значений.</li>
        <li><code>RemoteStateLayout</code> при сборке вызывает вашу лямбду <b>по разу на каждое состояние</b>.
        Каждый вызов становится веткой операции <code>StateLayout</code>, которая ссылается на это число; цвета,
        тексты и ваш <code>when</code> в ветке уже посчитаны в константы (видно во вкладке «Документ»).</li>
        <li><code>valueChangeAction(screen, RemoteEnum(next))</code> записывается как «присвой числу N».</li>
        <li>На устройстве плеер показывает ветку для текущего значения; тап меняет число, и ветка переключается
        мгновенно, офлайн, без сервера и без Kotlin.</li>
      </ol>
      <p><b>Итого:</b> переключение мгновенное, но каждое состояние добавляет в документ ветку; логика в лямбде
      выполняется один раз на сервере, а не при каждом тапе. С <code>rememberNamedRemoteEnum("screen", …)</code>
      у числа появляется имя (<code>USER:screen</code>), и экран может переключать и приложение (вкладка
      «Значения приложения»).</p>`,
  };

  function pick() {
    // ?lang= from a link (the Russian static pages link here with ?lang=ru) wins and is remembered.
    const asked = new URLSearchParams(location.search).get("lang");
    if (asked === "ru" || asked === "en") {
      try {
        localStorage.setItem("rcsb-lang", asked);
      } catch {
        // Not remembered when storage is blocked; the language still applies to this page.
      }
      return asked;
    }
    let saved = null;
    try {
      saved = localStorage.getItem("rcsb-lang");
    } catch {
      // Storage can be blocked; fall back to the browser's language.
    }
    if (saved === "ru" || saved === "en") return saved;
    return (navigator.language || "en").toLowerCase().startsWith("ru") ? "ru" : "en";
  }

  const I18n = {
    lang: pick(),
    t(key, vars) {
      const dict = I18n.lang === "ru" ? ru : en;
      let s = dict[key] ?? en[key] ?? key;
      if (vars) for (const [k, v] of Object.entries(vars)) s = s.split("{" + k + "}").join(String(v));
      return s;
    },
    category(name) {
      return I18n.lang === "ru" ? categoriesRu[name] || name : name;
    },
    // The component's plain-language description in the current language: {what, useWhen}.
    plain(component) {
      if (I18n.lang === "ru" && plainRu[component.id]) {
        const [what, useWhen] = plainRu[component.id];
        return { what, useWhen };
      }
      return component.plain;
    },
    // The story's name and description in the current language: {name, description}.
    story(story) {
      const ru = I18n.lang === "ru" && storiesRu[story.id];
      return ru ? { name: ru[0], description: ru[1] } : { name: story.name, description: story.description };
    },
    // The component's "how it works" explanation in the current language, or null.
    how(component) {
      return (I18n.lang === "ru" ? howRu : howEn)[component.id] || null;
    },
    set(lang) {
      I18n.lang = lang;
      document.documentElement.lang = lang;
      try {
        localStorage.setItem("rcsb-lang", lang);
      } catch {
        // Not remembered when storage is blocked; the choice still applies to this page.
      }
    },
    // Applies data-i18n (text) and data-i18n-<attr> (attributes) in `root`.
    apply(root) {
      root.querySelectorAll("[data-i18n]").forEach((el) => {
        el.textContent = I18n.t(el.dataset.i18n);
      });
      for (const attr of ["title", "placeholder", "aria-label"]) {
        root.querySelectorAll(`[data-i18n-${attr}]`).forEach((el) => {
          el.setAttribute(attr, I18n.t(el.getAttribute(`data-i18n-${attr}`)));
        });
      }
    },
    keys: { en, ru, plainRu, categoriesRu, storiesRu },
  };
  document.documentElement.lang = I18n.lang;
  window.RcI18n = I18n;
})();
