(() => {
  const letters = window.LETTER_SAMPLE.map(l => ({ ...l })); // 발행할 뉴스레터들

  const $ = id => document.getElementById(id);
  const form = $('sc-form');
  const daySelect = $('sc-day');
  const timeInput = $('sc-time');
  const listEl = $('sc-list');

  // ① 발행일 선택칸에 1~28일 넣기 (29~31일은 없는 달이 있어서 제외)
  for (let day = 1; day <= 28; day++) {
    const option = document.createElement('option');
    option.value = day;
    option.textContent = day;
    if (day === 10) option.selected = true; // 시안 기본값: 10일
    daySelect.append(option);
  }

  // '14:30' → '오후 2:30'
  function formatTime(value) {
    const [h, m] = value.split(':').map(Number);
    const ampm = h < 12 ? '오전' : '오후';
    const hour = h % 12 || 12;
    return `${ampm} ${hour}:${String(m).padStart(2, '0')}`;
  }

  // ② 뉴스레터 목록 그리기 (하나만 선택 가능, 예약된 건 취소 버튼)
  function render() {
    const checkedId = listEl.querySelector('input:checked')?.value; // 다시 그려도 선택 유지
    listEl.innerHTML = '';
    letters.forEach(letter => {
      const li = document.createElement('li');
      li.className = 'sc-item';

      const label = document.createElement('label');
      label.className = 'sc-option';

      const radio = document.createElement('input');
      radio.type = 'radio';
      radio.name = 'sc-letter';
      radio.value = letter.id;
      radio.checked = String(letter.id) === checkedId;

      const text = document.createElement('span');

      const title = document.createElement('span');
      title.className = 'sc-item-title';
      const no = document.createElement('span');
      no.className = 'sc-no';
      no.textContent = `${letter.no}호`;
      title.append(no, letter.title);

      const date = document.createElement('span');
      date.className = 'sc-item-date';
      date.textContent = letter.date;
      if (letter.reservedAt) {
        const badge = document.createElement('span');
        badge.className = 'sc-badge';
        badge.textContent = `예약됨 · ${letter.reservedAt}`;
        date.append(badge);
      }

      text.append(title, date);
      label.append(radio, text);
      li.append(label);

      // 예약된 뉴스레터에만 '예약 취소' 버튼
      if (letter.reservedAt) {
        const cancel = document.createElement('button');
        cancel.type = 'button';
        cancel.className = 'sc-cancel';
        cancel.dataset.id = letter.id;
        cancel.textContent = '예약 취소';
        li.append(cancel);
      }

      listEl.append(li);
    });
  }

  // ③ 예약하기
  form.addEventListener('submit', e => {
    e.preventDefault(); // 페이지 새로고침 막기
    const checked = listEl.querySelector('input:checked');
    if (!checked) {
      DaroriAdmin.showDialog({ title: '뉴스레터 선택', description: '발행할 뉴스레터를 하나 선택해 주세요.' });
      return;
    }
    if (!timeInput.value) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '발행 시간을 선택해 주세요.' });
      return;
    }

    const letter = letters.find(l => String(l.id) === checked.value);
    const when = `매월 ${daySelect.value}일 ${formatTime(timeInput.value)}`;

    DaroriAdmin.showDialog({
      title: '발행 예약',
      description: `${letter.no}호 '${letter.title}'을(를) ${when}에 발행하도록 예약할까요?`,
      confirmLabel: '예약하기',
      onConfirm: () => {
        letter.reservedAt = when;
        render();
        DaroriAdmin.showDialog({ title: '예약 완료', description: `${when}에 발행될 예정이에요.` });
      }
    });
  });

  // ④ 예약 취소
  listEl.addEventListener('click', e => {
    const btn = e.target.closest('.sc-cancel');
    if (!btn) return;
    const letter = letters.find(l => String(l.id) === btn.dataset.id);

    DaroriAdmin.showDialog({
      title: '예약 취소',
      description: `${letter.no}호 '${letter.title}'의 발행 예약을 취소할까요?`,
      confirmLabel: '예약 취소',
      onConfirm: () => {
        delete letter.reservedAt; // 예약 정보 지우기
        render();
      }
    });
  });

  render();
})();