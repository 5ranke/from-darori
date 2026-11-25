/* 홈 구독 입력 검증. 백엔드 미구현으로 API 호출·저장·신청 완료 처리는 하지 않습니다. */
(() => {
  const email = document.getElementById('home-subscription-email');
  const consent = document.getElementById('home-subscription-consent');
  const button = document.getElementById('home-subscription-button');
  const feedback = document.getElementById('home-subscription-feedback');
  if (!email || !consent || !button || !feedback) return;

  const pendingMessage = '구독 신청 기능 준비 중입니다. 입력한 정보는 전송·저장되지 않습니다.';
  let emailTouched = false;
  let consentTouched = false;

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

  // 향후 API 연동 시 사용할 요청 데이터 준비 경계입니다.
  // 유효하지 않으면 null을 반환하며, 반환값을 저장하거나 전송하지 않습니다.
  function prepareSubscriptionRequest(value, agreedToPrivacy) {
    if (validateEmail(value) || agreedToPrivacy !== true) return null;
    return { email: value.trim(), agreedToPrivacy: true };
  }

  function updateValidation() {
    const error = validateEmail(email.value);
    const ready = prepareSubscriptionRequest(email.value, consent.checked) !== null;
    email.setAttribute('aria-invalid', String(emailTouched && Boolean(error)));
    consent.setAttribute('aria-invalid', String(consentTouched && !consent.checked));
    // 입력 조건과 API 준비 상태는 별개입니다. 아직 실제 신청은 허용하지 않습니다.
    button.disabled = true;
    if (emailTouched && error) feedback.textContent = `${error} ${pendingMessage}`;
    else if ((emailTouched || consentTouched) && !consent.checked) {
      feedback.textContent = `개인정보 수집 및 이용에 동의해 주세요. ${pendingMessage}`;
    } else if (ready) {
      feedback.textContent = `입력 조건을 충족했습니다. 아직 구독 신청된 상태는 아닙니다. ${pendingMessage}`;
    } else feedback.textContent = pendingMessage;
  }

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

  function issueText(letter) {
    return `${letter.issueYear} ${letter.issueMonth}월호`;
  }

  function letterUrl(letter) {
    const params = new URLSearchParams({ newsletterId: letter.newsletterId });
    return `visitor/letter.html?${params}`;
  }

  function openStory(button, story, letter) {
    document.getElementById('home-story-category').textContent = story.categoryName;
    document.getElementById('home-story-date').textContent = issueText(letter);
    document.getElementById('home-story-title').textContent = story.title;
    document.getElementById('home-story-body').textContent = story.excerpt;
    document.getElementById('home-story-letter').href = letterUrl(letter);
    document.getElementById('home-story-letter-label').textContent = `${issueText(letter)} 보러가기`;
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
    if (!latestNewsletterId) return;
    const latest = recentNewsletters[0];

    latestLink.href = letterUrl(latest);
    latestLink.hidden = false;

    newsHeading.textContent = `${issueText(latest)} 다로리 소식`;
    for (const story of featuredStories) {
      const card = newsTemplate.content.firstElementChild.cloneNode(true);
      card.querySelector('.visitor-home-tag').textContent = story.categoryName;
      card.querySelector('.visitor-home-date').textContent = issueText(latest);
      card.querySelector('h3').textContent = story.title;
      card.querySelector('.visitor-home-excerpt').textContent = story.excerpt;
      const button = card.querySelector('button');
      button.dataset.homeStory = story.publishedStoryId;
      button.addEventListener('click', () => openStory(button, story, latest));
      newsGrid.append(card);
    }
    newsGrid.closest('section').hidden = featuredStories.length === 0;

    for (const letter of recentNewsletters) {
      const card = letterTemplate.content.firstElementChild.cloneNode(true);
      const thumbnail = card.querySelector('img');
      if (letter.thumbnailUrl) thumbnail.src = letter.thumbnailUrl;
      else thumbnail.remove();
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
