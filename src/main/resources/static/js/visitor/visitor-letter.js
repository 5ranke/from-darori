/* 편지 상세 화면 전용 예시 데이터입니다. 서버 응답이나 API 요청 모델이 아닙니다.
 * issue는 화면 예시를 고르는 키이며 newsletter.id(UUID)를 대신하는 API 식별자가 아닙니다.
 * API 계약 확인 후 예시 선택 부분을 교체하고 화면 렌더링을 재사용할 수 있습니다.
 * 제공 시안에 별도 마무리 문구가 없어 임의의 closing을 추가하지 않습니다.
 */
(() => {
  const sectionsHost = document.getElementById('letter-sections');
  if (!sectionsHost) return;

  // 홈과 동일한 발행 정보만 존재하는 10·9월호에 11월호 본문을 재사용하지 않습니다.
  const examples = {
    '2026-11': {
      issue: '2026 11월호 | 최신',
      title: '다로리의 다음 장, 다시 만날 기회를 전해요',
      intro: '마을의 늦가을 안부와 참여 프로젝트의 변화, 새롭게 열린 참여 기회와 제철 먹거리를 한 통에 담았습니다.',
      sections: [
        {
          category: '다로리의 요즘',
          title: '마을회관에서 나눈 늦가을 안부',
          paragraphs: [
            '수확을 마친 주민들이 마을회관에 모여 따뜻한 차를 나누었습니다. 함께 정리한 마당에는 겨울을 준비하는 손길이 이어졌어요. 오래된 마을 사진을 펼쳐 놓고 사진 속 장소와 이웃의 이야기를 들었습니다.',
            '다음 모임에는 집에 보관하던 사진을 더 가져와 작은 마을 앨범을 만들어 보기로 했습니다.'
          ],
          photoSlots: 3
        },
        {
          category: '그 프로젝트, 지금은?',
          icon: '🌱',
          title: '여러분이 남긴 산책 기록, 마을 사진 지도로 이어졌어요',
          paragraphs: [
            '지난 마을길 기록 프로젝트에서 방문자들이 남긴 사진과 메모를 주민들과 함께 정리했습니다. 돌담길과 느티나무, 잠시 쉬어 갔던 쉼터를 한 장의 사진 지도에 담았어요. 주민들은 장소마다 얽힌 기억을 덧붙여 주셨습니다.',
            '다음 단계에서는 계절별 사진을 더 모아 마을회관에서 누구나 펼쳐볼 수 있는 기록으로 이어갈 예정입니다.'
          ],
          photoSlots: 3
        },
        {
          category: '다로리에서 찾습니다',
          icon: '🙋🏻‍♀️',
          title: '마을 사진 교실을 함께할 강사를 찾습니다',
          paragraphs: [
            '주민들이 스마트폰으로 일상을 찍고 사진을 정리하는 방법을 배울 수 있도록 사진 교실 강사를 모집하는 예시입니다. 수업은 마을회관에서 소규모로 진행하며 풍경 촬영과 사진 앨범 정리를 다룹니다.'
          ],
          linkLabel: '신청 링크'
        },
        {
          category: '다로리의 제철',
          icon: '🍊',
          title: '가을 감으로 전하는 다로리의 제철',
          paragraphs: [
            '이번 제철 코너에서는 가을 감을 소개합니다. 잘 익은 감을 이웃과 나누는 일도 마을의 계절 풍경 중 하나예요. 실제 판매를 시작할 때는 생산자, 품종, 중량과 가격, 보관 방법을 이곳에서 안내하고 구매 링크를 연결할 예정입니다.'
          ],
          linkLabel: '구매 링크',
          photoSlots: 3
        }
      ]
    },
    '2026-10': { issue: '2026 10월호', title: '마을길의 다음 계절을 함께 걸어요' },
    '2026-09': { issue: '2026 9월호', title: '지난 만남의 기록을 모아, 다로리의 겨울을 준비해요' }
  };

  function element(tag, className, text) {
    const node = document.createElement(tag);
    if (className) node.className = className;
    if (text) node.textContent = text;
    return node;
  }

  function renderLetter(example) {
    document.getElementById('letter-issue').textContent = example ? example.issue : '발행호 확인 필요';
    const title = example ? example.title : '편지를 찾을 수 없습니다';
    document.getElementById('letter-title').textContent = title;
    document.title = `${title} | From. 다로리`;
    document.getElementById('letter-intro').textContent = example?.intro || '';
    const unavailable = document.getElementById('letter-unavailable');
    unavailable.hidden = Boolean(example?.sections);
    unavailable.textContent = example
      ? '이 발행호의 상세 예시 데이터는 아직 준비되지 않았습니다.'
      : '요청한 발행호의 예시 데이터가 없습니다.';
    const quiz = document.getElementById('letter-quiz');
    const sidebar = document.getElementById('letter-sidebar');
    const hasSections = Boolean(example?.sections);
    quiz.hidden = !hasSections;
    sidebar.hidden = !hasSections;
    document.getElementById('letter-layout').classList.toggle('visitor-letter-layout--with-sidebar', hasSections);
    sectionsHost.replaceChildren();
    const destinations = [];
    for (const [index, data] of (example?.sections || []).entries()) {
      const section = element('section', 'visitor-letter-section');
      const heading = element('h2', '', data.icon ? `${data.icon} ${data.category}` : data.category);
      heading.id = `letter-section-${index + 1}`;
      heading.tabIndex = -1;
      destinations.push([heading.id, data.category]);
      section.setAttribute('aria-labelledby', heading.id);
      section.append(heading, element('h3', '', data.title));
      for (const text of data.paragraphs) section.append(element('p', '', text));
      if (data.linkLabel) {
        const link = element('p', 'visitor-letter-placeholder-link', data.linkLabel);
        // 시안의 https://www.link.com은 예시 주소입니다. 외부 링크로 연결하지 않습니다.
        link.append(element('span', '', 'https://www.link.com'));
        section.append(link);
      }
      if (data.photoSlots) {
        const photos = element('div', 'visitor-letter-photos');
        for (let slot = 1; slot <= data.photoSlots; slot++) {
          // API 미연동 상태의 Figma 예시 이미지. 시안과 같이 동일한 건물 사진을 반복합니다.
          const photo = element('img', 'visitor-letter-photo');
          photo.src = '../images/visitor/cafe-darori.png';
          photo.alt = `카페 다로리 건물 외관과 입구 계단 — 예시 사진 ${slot}`;
          photos.append(photo);
        }
        section.append(photos);
      }
      sectionsHost.append(section);
    }
    if (hasSections) {
      destinations.push([quiz.id, '다로리 퀴즈']);
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
  }

  const issue = new URLSearchParams(window.location.search).get('issue') ?? '2026-11';
  // 상속 속성명이나 임의의 쿼리를 예시 데이터로 해석하지 않습니다.
  renderLetter(Object.hasOwn(examples, issue) ? examples[issue] : null);
})();
