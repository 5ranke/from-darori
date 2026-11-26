(() => {
  const sectionsHost = document.getElementById('letter-sections');
  const sidebar = document.getElementById('letter-sidebar');
  const status = document.getElementById('letter-unavailable');
  const quiz = document.getElementById('letter-quiz');
  const categoryEmoji = new Map([
    ['프로젝트 후속', '🌱'],
    ['그 프로젝트, 지금은?', '🌱'],
    ['그 프로젝트 지금은?', '🌱'],
    ['구인/참여', '🙋'],
    ['모집', '🙋'],
    ['다로리에서 찾습니다', '🙋'],
    ['제철 정보', '🍊'],
    ['특산물', '🍊'],
    ['다로리의 제철', '🍊']
  ]);

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text) node.textContent = text;
    return node;
  }

  function webUrl(value) {
    if (!value) return null;
    try {
      const url = new URL(value, window.location.origin);
      return url.protocol === 'https:' || url.protocol === 'http:' ? url.href : null;
    } catch {
      return null;
    }
  }

  function renderStory(story) {
    const article = element('article', 'visitor-letter-story');
    article.append(element('h3', '', story.title), element('p', 'visitor-letter-body', story.body));
    if (story.applicationDeadline) {
      article.append(element('p', '', `모집 마감일 ${story.applicationDeadline}`));
    }
    const actionUrl = webUrl(story.actionUrl);
    if (actionUrl) {
      const link = element('a', 'visitor-letter-action', '관련 링크 보기');
      link.href = actionUrl;
      article.append(link);
    }
    const photos = element('div', 'visitor-letter-photos');
    for (const image of story.images) {
      const src = webUrl(image.url);
      if (!src) continue;
      const photo = element('img', 'visitor-letter-photo');
      photo.src = src;
      photo.alt = image.altText ?? '';
      photo.loading = 'lazy';
      photos.append(photo);
    }
    if (photos.childElementCount) article.append(photos);
    return article;
  }

  function renderQuiz(data) {
    if (!data) return;
    const fieldset = quiz.querySelector('fieldset');
    fieldset.append(element('legend', '', `Q. ${data.question}`));
    data.choices.forEach((choice, index) => {
      const label = element('label');
      const input = element('input');
      input.type = 'radio';
      input.name = 'letter-quiz-answer';
      input.value = String(index);
      label.append(input, choice);
      fieldset.append(label);
    });
    quiz.hidden = false;
  }

  function renderSidebar(destinations) {
    if (!destinations.length) return;
    sidebar.hidden = false;
    document.getElementById('letter-layout').classList.add('visitor-letter-layout--with-sidebar');
    const links = destinations.map(([id, label]) => {
      const link = element('a', 'visitor-letter-menu', label);
      link.href = `#${id}`;
      return link;
    });
    const targets = destinations.map(([id]) => document.getElementById(id));
    let activeLink;
    function activate(link) {
      if (link === activeLink) return;
      activeLink?.removeAttribute('aria-current');
      link.setAttribute('aria-current', 'location');
      activeLink = link;
    }

    let framePending = false;
    function updateOnScroll() {
      if (framePending) return;
      framePending = true;
      window.requestAnimationFrame(() => {
        framePending = false;
        const viewportHeight = document.documentElement.clientHeight;
        const readingLine = Math.min(96, viewportHeight / 4);
        const index = targets.findLastIndex(target => target.getBoundingClientRect().top <= readingLine);
        // The last section may reach the bottom before its heading crosses the reading line.
        const atBottom = window.scrollY > 0
          && window.scrollY + viewportHeight >= document.documentElement.scrollHeight - 1;
        activate(atBottom ? links[links.length - 1] : links[Math.max(0, index)]);
      });
    }

    sidebar.replaceChildren(...links);
    activate(links[0]);
    for (const link of links) link.addEventListener('click', () => activate(link));
    window.addEventListener('scroll', updateOnScroll, { passive: true });
    window.addEventListener('resize', updateOnScroll);
    updateOnScroll();
  }

  function renderLetter(letter) {
    const badge = letter.isLatest ? ' | 최신' : '';
    document.getElementById('letter-issue').textContent = `${letter.issueYear} ${letter.issueMonth}월호${badge}`;
    document.getElementById('letter-title').textContent = letter.title;
    document.title = `${letter.title} | From. 다로리`;
    document.getElementById('letter-intro').textContent = letter.summary ?? '';

    const destinations = letter.sections.map((data, index) => {
      const section = element('section', 'visitor-letter-section');
      const emoji = categoryEmoji.get(data.title);
      const label = emoji ? `${emoji} ${data.title}` : data.title;
      const heading = element('h2', '', label);
      heading.id = `letter-section-${index + 1}`;
      heading.tabIndex = -1;
      section.setAttribute('aria-labelledby', heading.id);
      section.append(heading, ...data.stories.map(renderStory));
      sectionsHost.append(section);
      return [heading.id, data.title];
    });
    renderQuiz(letter.quiz);
    if (letter.quiz) destinations.push([quiz.id, '다로리 퀴즈']);
    renderSidebar(destinations);
  }

  async function loadLetter() {
    const newsletterId = new URLSearchParams(window.location.search).get('newsletterId');
    if (!newsletterId) {
      status.textContent = '열어볼 편지를 선택해 주세요.';
      return;
    }
    try {
      const response = await fetch(`/api/v1/newletters/${encodeURIComponent(newsletterId)}`, {
        headers: { Accept: 'application/json' }
      });
      if (response.status === 400 || response.status === 404) {
        status.textContent = '편지를 찾을 수 없습니다.';
        return;
      }
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const { isSuccess, result } = await response.json();
      if (!isSuccess || !result) throw new Error('Invalid newsletter response');
      renderLetter(result);
      status.hidden = true;
    } catch {
      status.textContent = '편지를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
    }
  }

  loadLetter();
})();
