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

/* 홈 소식 상세 팝업 전용. Figma 예시 데이터이며 API를 호출하거나 저장하지 않습니다.
 * photo-map만 상세 시안의 전체 본문·사진을 제공합니다.
 * 나머지 카드는 확인 가능한 기존 카드 문구만 사용하며 상세 내용을 임의로 만들지 않습니다.
 */
(() => {
  const dialog = document.getElementById('home-story-dialog');
  if (!dialog || typeof dialog.showModal !== 'function') return;

  const category = document.getElementById('home-story-category');
  const date = document.getElementById('home-story-date');
  const title = document.getElementById('home-story-title');
  const body = document.getElementById('home-story-body');
  const images = document.getElementById('home-story-images');
  const letterLabel = document.getElementById('home-story-letter-label');
  const closeButton = document.getElementById('home-story-close');
  let opener = null;

  const details = {
    'photo-map': {
      paragraphs: [
        '지난 마을길 기록 프로젝트에서 방문자들이 남긴 사진과 메모를 주민들과 함께 정리했습니다. 돌담길과 느티나무, 잠시 쉬어 갔던 쉼터를 한 장의 사진 지도에 담았어요. 주민들은 장소마다 얽힌 기억을 덧붙여 주셨습니다.',
        '다음 단계에서는 계절별 사진을 더 모아 마을회관에서 누구나 펼쳐볼 수 있는 기록으로 이어갈 예정입니다.'
      ],
      images: [
        { src: 'images/visitor/letter-2026-11.png', alt: '감이 열린 감나무와 돌담 앞에 놓인 감 상자' },
        { src: 'images/visitor/letter-2026-10.png', alt: '채소 사이 빨간 바구니에 앉아 있는 흰 강아지' },
        { src: 'images/visitor/letter-2026-09.png', alt: '눈 덮인 마을길을 걸어가는 사람' }
      ]
    }
  };

  function openStory(button) {
    if (dialog.open) return;
    const card = button.closest('.visitor-home-news-card');
    const detail = details[button.dataset.homeStory];
    // 카드의 태그·날짜·제목과 동일한 내용을 표시합니다. HTML 문자열은 삽입하지 않습니다.
    category.textContent = card.querySelector('.visitor-home-tag').textContent;
    date.textContent = card.querySelector('.visitor-home-date').textContent;
    title.textContent = card.querySelector('h3').textContent;
    const paragraphs = detail ? detail.paragraphs : [card.querySelector('.visitor-home-excerpt').textContent];
    body.replaceChildren(...paragraphs.map(text => {
      const paragraph = document.createElement('p');
      paragraph.textContent = text;
      return paragraph;
    }));
    const photos = detail ? detail.images : [];
    images.replaceChildren(...photos.map(photo => {
      const image = document.createElement('img');
      image.src = photo.src;
      image.alt = photo.alt;
      image.width = 252;
      image.height = 190;
      return image;
    }));
    images.hidden = photos.length === 0;
    images.scrollLeft = 0;
    letterLabel.textContent = `${date.textContent} 보러가기`;
    opener = button;
    dialog.showModal();
    dialog.scrollTop = 0;
    document.body.classList.add('visitor-home--modal-open');
    closeButton.focus();
  }

  closeButton.addEventListener('click', () => dialog.close());
  // Escape로 닫힐 때도 동일하게 배경 스크롤과 원래 버튼의 포커스를 복구합니다.
  dialog.addEventListener('close', () => {
    document.body.classList.remove('visitor-home--modal-open');
    if (opener && opener.isConnected) opener.focus();
    opener = null;
  });
  document.querySelectorAll('[data-home-story]').forEach(button => {
    button.addEventListener('click', () => openStory(button));
    button.disabled = false;
    button.removeAttribute('title');
  });
})();
