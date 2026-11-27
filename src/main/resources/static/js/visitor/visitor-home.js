(() => {
  const form = document.getElementById('home-subscription-form');
  const email = document.getElementById('home-subscription-email');
  const consent = document.getElementById('home-subscription-consent');
  const button = document.getElementById('home-subscription-button');
  const feedback = document.getElementById('home-subscription-feedback');
  if (!form || !email || !consent || !button || !feedback) return;

  const enabled = form.dataset.subscriptionEnabled === 'true';
  const pendingMessage = '구독 신청 기능 준비 중입니다. 입력한 정보는 전송·저장되지 않습니다.';
  const consentError = '개인정보 수집 및 이용에 동의해 주세요.';
  const submitLabel = button.textContent;
  let emailTouched = false;
  let consentTouched = false;
  let submitting = false;
  if (enabled) button.removeAttribute('title');

  function validateEmail(value) {
    const normalizedEmail = value.trim();
    if (!normalizedEmail) return '이메일 주소를 입력해 주세요.';
    if (normalizedEmail.length > 254) return '이메일 주소는 최대 254자까지 입력해 주세요.';
    // 실제 입력란과 같은 HTML 이메일 형식 검사를 사용합니다. 대소문자는 변경하지 않습니다.
    const checker = document.createElement('input');
    checker.type = 'email';
    checker.value = normalizedEmail;
    if (!checker.checkValidity()) return '올바른 이메일 주소를 입력해 주세요.';
    return '';
  }

  function updateValidation(message) {
    if (submitting) return;
    const error = validateEmail(email.value);
    email.setAttribute('aria-invalid', String(emailTouched && Boolean(error)));
    consent.setAttribute('aria-invalid', String(consentTouched && !consent.checked));
    button.disabled = !enabled || Boolean(error) || !consent.checked;
    const validation = emailTouched && error ? error
      : (emailTouched || consentTouched) && !consent.checked ? consentError : '';
    const status = message ?? validation;
    feedback.textContent = enabled ? status : [status, pendingMessage].filter(Boolean).join(' ');
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    if (!enabled || submitting) return;

    emailTouched = consentTouched = true;
    const error = validateEmail(email.value);
    if (error || !consent.checked) {
      updateValidation();
      (error ? email : consent).focus();
      return;
    }

    submitting = true;
    button.disabled = true;
    email.readOnly = true;
    consent.disabled = true;
    button.textContent = '신청 중…';
    feedback.textContent = '구독 신청을 처리하고 있어요.';
    let message;
    try {
      const response = await fetch('/api/v1/subscriptions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ email: email.value.trim(), agreedToPrivacy: true })
      });
      const data = await response.json().catch(() => null);
      if (response.ok && data?.isSuccess === true) {
        message = '뉴스레터 구독이 완료됐어요.';
        form.reset();
        emailTouched = consentTouched = false;
      } else if (response.status === 400) {
        message = data?.code === 'SUBSCRIBER4001'
          ? '이미 구독 중인 이메일이에요.'
          : '이메일 주소와 개인정보 동의를 확인해 주세요.';
      } else {
        message = '구독 신청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
      }
    } catch {
      message = '서버에 연결하지 못했어요. 인터넷 연결을 확인한 뒤 다시 시도해 주세요.';
    } finally {
      submitting = false;
      email.readOnly = false;
      consent.disabled = false;
      button.textContent = submitLabel;
      if (form.contains(document.activeElement) || document.activeElement === document.body) feedback.focus();
      updateValidation(message);
    }
  });

  email.addEventListener('input', () => { emailTouched = true; updateValidation(); });
  email.addEventListener('blur', () => {
    emailTouched = true;
    email.value = email.value.trim();
    updateValidation();
  });
  consent.addEventListener('change', () => { consentTouched = true; updateValidation(); });
  updateValidation();
})();

(() => {
  const latestLink = document.getElementById('home-latest-letter');
  const newsGrid = document.getElementById('home-news-grid');
  const newsHeading = document.getElementById('home-news-heading');
  const letterList = document.getElementById('home-letter-list');
  const newsTemplate = document.getElementById('home-news-template');
  const letterTemplate = document.getElementById('home-letter-template');
  const status = document.getElementById('home-content-status');
  const dialog = document.getElementById('home-story-dialog');
  const closeButton = document.getElementById('home-story-close');
  let opener;
  let storyRequestId = 0;

  const categoryEmoji = new Map([
    ['구인/참여', '🙋'],
    ['구인 참여', '🙋'],
    ['프로젝트 후속', '🌱'],
    ['제철 정보', '🍊']
  ]);

  function categoryLabel(name) {
    const emoji = categoryEmoji.get(name);
    return emoji ? `${emoji} ${name}` : name;
  }

  function issueText(letter) {
    return `${letter.issueYear} ${letter.issueMonth}월호`;
  }

  function letterUrl(letter) {
    const params = new URLSearchParams({ newsletterId: letter.newsletterId });
    return `visitor/letter.html?${params}`;
  }

  async function openStory(button, publishedStoryId) {
    const requestId = ++storyRequestId;
    try {
      const response = await fetch(`/api/v1/published-stories/${encodeURIComponent(publishedStoryId)}`, {
        headers: { Accept: 'application/json' }
      });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const { isSuccess, result } = await response.json();
      if (!isSuccess || !result) throw new Error('Invalid published story response');
      if (requestId === storyRequestId) {
        status.textContent = '';
        renderStory(button, result);
      }
    } catch {
      if (requestId === storyRequestId) {
        status.textContent = '소식을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
      }
    }
  }

  function renderStory(button, story) {
    const issue = issueText(story);
    document.getElementById('home-story-category').textContent = categoryLabel(story.categoryName);
    document.getElementById('home-story-date').textContent = issue;
    document.getElementById('home-story-title').textContent = story.title;
    document.getElementById('home-story-body').textContent = story.body;
    document.getElementById('home-story-letter').href = letterUrl(story);
    document.getElementById('home-story-letter-label').textContent = `${issue} 보러가기`;

    const images = document.getElementById('home-story-images');
    images.replaceChildren(...story.images.filter(image => image.url).map(image => {
      const img = document.createElement('img');
      img.addEventListener('error', () => {
        img.remove();
        images.hidden = !images.childElementCount;
      }, { once: true });
      img.src = image.url;
      img.alt = image.altText ?? '';
      img.loading = 'lazy';
      return img;
    }));
    images.hidden = !images.childElementCount;
    images.scrollLeft = 0;

    const deadline = document.getElementById('home-story-deadline');
    deadline.hidden = !story.applicationDeadline;
    deadline.textContent = story.applicationDeadline ? `모집 마감일 · ${story.applicationDeadline}` : '';

    const action = document.getElementById('home-story-action');
    action.hidden = !/^https?:\/\//i.test(story.actionUrl ?? '');
    if (action.hidden) action.removeAttribute('href');
    else action.href = story.actionUrl;

    opener = button;
    dialog.showModal();
    dialog.scrollTop = 0;
    document.body.classList.add('visitor-home--modal-open');
    closeButton.focus();
  }

  closeButton.addEventListener('click', () => dialog.close());
  dialog.addEventListener('close', () => {
    document.body.classList.remove('visitor-home--modal-open');
    opener?.focus();
  });

  function renderHome({ latestNewsletterId, featuredStories, recentNewsletters }) {
    if (!latestNewsletterId) {
      status.textContent = '아직 발행된 다로리 편지가 없어요. 새 편지가 발행되면 이곳에서 만나보실 수 있어요.';
      letterList.hidden = true;
      letterList.closest('section').hidden = false;
      return;
    }
    const latest = recentNewsletters[0];

    latestLink.href = letterUrl(latest);
    latestLink.hidden = false;

    newsHeading.textContent = `${issueText(latest)} 다로리 소식`;
    for (const story of featuredStories) {
      const card = newsTemplate.content.firstElementChild.cloneNode(true);
      card.querySelector('.visitor-home-tag').textContent = categoryLabel(story.categoryName);
      card.querySelector('.visitor-home-date').textContent = issueText(latest);
      card.querySelector('h3').textContent = story.title;
      card.querySelector('.visitor-home-excerpt').textContent = story.excerpt;
      const button = card.querySelector('button');
      button.dataset.homeStory = story.publishedStoryId;
      button.addEventListener('click', () => openStory(button, story.publishedStoryId));
      newsGrid.append(card);
    }
    newsGrid.closest('section').hidden = featuredStories.length === 0;

    for (const letter of recentNewsletters) {
      const card = letterTemplate.content.firstElementChild.cloneNode(true);
      const thumbnail = card.querySelector('img');
      if (letter.thumbnailUrl) {
        thumbnail.addEventListener('error', () => thumbnail.remove(), { once: true });
        thumbnail.src = letter.thumbnailUrl;
      } else thumbnail.remove();
      const badge = letter.newsletterId === latestNewsletterId ? ' | 최신' : '';
      card.querySelector('.visitor-home-tag').textContent = issueText(letter) + badge;
      card.querySelector('h3').textContent = letter.title;
      card.querySelector('p').textContent = letter.summary ?? '';
      card.querySelector('a').href = letterUrl(letter);
      letterList.append(card);
    }
    letterList.closest('section').hidden = false;
  }

  async function loadHome() {
    try {
      const response = await fetch('/api/v1/home', { headers: { Accept: 'application/json' } });
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      const { isSuccess, result } = await response.json();
      if (!isSuccess || !result) throw new Error('Invalid home response');
      renderHome(result);
    } catch {
      status.textContent = '다로리 소식을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.';
    }
  }

  loadHome();
})();
