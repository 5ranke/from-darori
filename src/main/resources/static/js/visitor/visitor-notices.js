(() => {
  const categories = { GUIDE: '안내', NEWS: '소식', PROGRAM: '프로그램', CLOSED: '마감' };
  // 화면 검증용 예시 데이터입니다. API 응답 계약이나 실제 공지 내용과는 별개입니다.
  const sampleNotices = [
    {
      id: 'launch', category: 'NEWS', isPinned: true,
      title: '다로리 편지 홈페이지가 새롭게 문을 열었습니다',
      body: '다로리 편지 홈페이지에서 마을의 새로운 소식과 지난 편지를 읽어 보세요.'
    },
    {
      id: 'visit', category: 'GUIDE', isPinned: true,
      title: '협업 및 방문 문의 안내',
      body: '협업이나 단체 방문을 계획 중이시라면 방문 전에 문의해 주세요.'
    },
    {
      id: 'cafe', category: 'GUIDE',
      title: '카페 다로리 방문 및 이용 시간 안내',
      body: `안녕하세요, 다로리입니다. 카페 다로리를 찾아 주시는 분들께 방문 전에 알아두시면 좋은 내용을 안내드려요.

<운영 시간>
평일(화~금): 오전 10시 ~ 오후 6시 (마지막 주문 오후 5시 30분)
주말(토~일): 오전 11시 ~ 오후 5시 (마지막 주문 오후 4시 30분)
정기 휴무: 매주 월요일
설과 추석 연휴에는 쉬고, 그 밖의 공휴일에는 주말 시간으로 운영해요.

<찾아오시는 길>
카페 다로리는 예전 삼신보건진료소였던 건물을 다시 가꾼 공간이에요. 경북 청도군 화양읍 다로리에 있고, 건물 앞에 차량 3~4대 정도 세울 수 있는 주차 공간이 있어요. 마을 안길은 좁고 아이들과 어르신이 자주 다니시니 천천히 운전해 주세요.

<이용 안내>
이곳은 마을 분들의 사랑방이자 아이들이 배우고 노는 교육돌봄 공간이기도 해요. 프로그램이 진행되는 시간에는 일부 좌석 이용이 제한될 수 있고, 아이들이 함께 있는 공간이라 조용하고 따뜻한 분위기를 함께 만들어 주시면 감사하겠습니다. 단체 방문이나 견학을 원하시면 미리 연락 주세요.

<문의>
대표 이메일 또는 인스타그램 @darori_in으로 편하게 문의해 주세요.

운영 시간은 상황에 따라 바뀔 수 있어요. 가장 최근 소식은 인스타그램에서 확인해 주세요. 따뜻한 차 한 잔 하러 놀러 오세요.`
    },
    {
      id: 'care', category: 'PROGRAM',
      title: '교육돌봄 프로그램 참여 안내',
      body: '교육돌봄 프로그램 참여를 희망하시는 분은 운영팀에 문의해 주세요.'
    },
    {
      id: 'award', category: 'NEWS',
      title: '2026 행복농촌만들기 콘테스트 금상 수상 소식',
      body: '다로리가 행복농촌만들기 콘테스트에서 금상을 받았습니다. 함께해 주신 분들께 감사드립니다.'
    },
    {
      id: 'youth', category: 'PROGRAM',
      title: '청년 프로그램 참여 안내',
      body: '청년 프로그램 참여에 관심이 있으시면 운영팀에 문의해 주세요.'
    }
  ].map(notice => ({ createdAt: '2026-11-10', isPinned: false, ...notice }));

  function listNotices(category, query) {
    const keyword = query.toLocaleLowerCase('ko-KR');
    return sampleNotices
      .filter(notice => (!category || notice.category === category)
        && notice.title.toLocaleLowerCase('ko-KR').includes(keyword))
      .sort((a, b) => Number(b.isPinned) - Number(a.isPinned) || b.createdAt.localeCompare(a.createdAt))
      .map(({ id, category, isPinned, title, createdAt }) => ({ id, category, isPinned, title, createdAt }));
  }

  function getNotice(id) {
    return sampleNotices.find(notice => notice.id === id);
  }

  const filters = [...document.querySelectorAll('.visitor-notices-filters button')];
  const form = document.querySelector('.visitor-notices-search');
  const list = document.querySelector('.visitor-notices-list');
  const status = document.querySelector('.visitor-notices-status');
  let category = '';
  let query = '';
  let closeExpandedNotice;

  function node(tag, className, text) {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text) element.textContent = text;
    return element;
  }

  function pinIcon() {
    const namespace = 'http://www.w3.org/2000/svg';
    const icon = document.createElementNS(namespace, 'svg');
    icon.setAttribute('viewBox', '0 0 24 24');
    icon.setAttribute('aria-hidden', 'true');
    const path = document.createElementNS(namespace, 'path');
    path.setAttribute('d', 'm8 16-5 5m15-9 2-2a2 2 0 0 0 0-3l-3-3a2 2 0 0 0-3 0l-2 2-5 1a2 2 0 0 0-1 3l8 8a2 2 0 0 0 3-1z');
    path.setAttribute('fill', 'none');
    path.setAttribute('stroke', 'currentColor');
    path.setAttribute('stroke-width', '2');
    path.setAttribute('stroke-linecap', 'round');
    path.setAttribute('stroke-linejoin', 'round');
    icon.append(path);
    return icon;
  }

  function renderNotice(notice) {
    const item = node('li', 'visitor-notice');
    const row = node('button', 'visitor-notice-row');
    const details = node('div', 'visitor-notice-details');
    row.type = 'button';
    row.id = `notice-${notice.id}`;
    details.id = `${row.id}-details`;
    details.hidden = true;
    row.setAttribute('aria-expanded', 'false');
    row.setAttribute('aria-controls', details.id);
    const label = node('span', 'visitor-notice-category');
    if (notice.isPinned) {
      item.classList.add('visitor-notice--pinned');
      const pinned = node('span', 'visitor-notices-sr-only', '고정 공지 ');
      label.append(pinIcon(), pinned);
    }
    label.append(document.createTextNode(categories[notice.category]));
    const date = node('time', '', notice.createdAt.replaceAll('-', '.'));
    date.dateTime = notice.createdAt;
    row.append(label, node('span', 'visitor-notice-title', notice.title), date);

    const close = node('button', 'visitor-notice-close');
    close.type = 'button';
    close.setAttribute('aria-label', `${notice.title} 본문 접기`);
    const body = node('p');
    details.append(body, close);
    item.append(row, details);

    function collapse() {
      details.hidden = true;
      row.setAttribute('aria-expanded', 'false');
      closeExpandedNotice = undefined;
    }

    row.addEventListener('click', () => {
      if (!details.hidden) {
        collapse();
        return;
      }
      closeExpandedNotice?.();
      body.textContent = getNotice(notice.id).body;
      details.hidden = false;
      row.setAttribute('aria-expanded', 'true');
      closeExpandedNotice = collapse;
    });
    close.addEventListener('click', () => {
      collapse();
      row.focus();
    });
    return item;
  }

  function renderList() {
    closeExpandedNotice = undefined;
    const notices = listNotices(category, query);
    list.replaceChildren(...notices.map(renderNotice));
    status.textContent = notices.length ? '' : '조건에 맞는 공지사항이 없습니다.';
  }

  for (const filter of filters) {
    filter.addEventListener('click', () => {
      category = filter.dataset.category;
      for (const button of filters) button.setAttribute('aria-pressed', String(button === filter));
      renderList();
    });
  }
  form.addEventListener('submit', event => {
    event.preventDefault();
    query = form.elements.query.value.trim();
    renderList();
  });
  renderList();
})();
