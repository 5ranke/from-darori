(() => {
  const api = window.ScheduleApi;

  let letters = [];        // 화면에 보이는 뉴스레터 (저장됨 + 예약됨)
  let savedCycle = null;   // 서버에 저장된 발행 주기 { day, time: 'HH:mm:ss' }
  const reservedAt = {};   // 이번에 예약한 뉴스레터의 발행 시각 (목록엔 시각이 안 와서 기억해 둠)
  let busy = false;

  const $ = id => document.getElementById(id);
  const form = $('sc-form');
  const daySelect = $('sc-day');
  const timeInput = $('sc-time');
  const listEl = $('sc-list');
  const listStatus = $('sc-list-status');
  const submitBtn = $('sc-submit');
  const saveCycleBtn = $('sc-save-cycle');
  const previewDialog = $('sc-preview-dialog');
  const previewStatus = $('sc-preview-status');
  const previewFrame = $('sc-preview-frame');
  let previewRevision = 0;

  // ① 발행일 선택칸에 1~31일 넣기
  for (let day = 1; day <= 31; day++) {
    const option = document.createElement('option');
    option.value = day;
    option.textContent = day;
    if (day === 10) option.selected = true;
    daySelect.append(option);
  }

  // 에러 처리: 로그인 만료면 로그인 화면으로, 아니면 팝업
  function handleError(error, title = '요청 실패') {
    if (error.kind === 'session') {
      window.location.replace('login.html?reason=expired');
      return;
    }
    DaroriAdmin.showDialog({ title, description: error.message || '요청을 처리하지 못했습니다.' });
  }

  // '14:30' → '오후 2:30'
  function formatTime(value) {
    const [h, m, seconds] = value.split(':').map(Number);
    const ampm = h < 12 ? '오전' : '오후';
    const hour = h % 12 || 12;
    return `${ampm} ${hour}:${String(m).padStart(2, '0')}${seconds ? ':' + String(seconds).padStart(2, '0') : ''}`;
  }
  // '2026-11-14T14:00:00+09:00' → '2026.11.14'
  const formatDate = iso => (iso ? iso.slice(0, 10).replaceAll('-', '.') : '');
  // '2026-12-10T10:00:00+09:00' → '2026.12.10 오전 10:00'
  const formatDateTime = iso => `${formatDate(iso)} ${formatTime(iso.slice(11, 19))}`;

  // ② 뉴스레터 목록 그리기
  function render() {
    const checkedId = listEl.querySelector('input:checked')?.value; // 다시 그려도 선택 유지
    listEl.innerHTML = '';
    letters.forEach(letter => {
      const isScheduled = letter.status === 'SCHEDULED';
      const li = document.createElement('li');
      li.className = 'sc-item';

      const label = document.createElement('label');
      label.className = 'sc-option';

      const radio = document.createElement('input');
      radio.type = 'radio';
      radio.name = 'sc-letter';
      radio.value = letter.newsletterId;
      radio.disabled = isScheduled; // 이미 예약된 건 다시 예약 불가
      radio.checked = !isScheduled && letter.newsletterId === checkedId;

      const text = document.createElement('span');

      const title = document.createElement('span');
      title.className = 'sc-item-title';
      if (letter.issueNumber) { // 호수는 발행 후에 생겨서 없으면 생략
        const no = document.createElement('span');
        no.className = 'sc-no';
        no.textContent = `${letter.issueNumber}호`;
        title.append(no);
      }
      title.append(letter.title);

      const date = document.createElement('span');
      date.className = 'sc-item-date';
      date.textContent = formatDate(letter.createdAt);
      if (isScheduled) {
        const badge = document.createElement('span');
        badge.className = 'sc-badge';
        const time = reservedAt[letter.newsletterId];
        badge.textContent = time ? `예약됨 · ${formatDateTime(time)}` : '예약됨';
        date.append(badge);
      }

      text.append(title, date);
      label.append(radio, text);
      li.append(label);

      const preview = document.createElement('button');
      preview.type = 'button';
      preview.className = 'admin-button admin-button--outline sc-preview';
      preview.dataset.id = letter.newsletterId;
      preview.textContent = '미리보기';
      preview.setAttribute('aria-haspopup', 'dialog');
      preview.setAttribute('aria-label', `${letter.title} 미리보기`);
      li.append(preview);

      // 예약된 뉴스레터에만 '예약 취소' 버튼
      if (isScheduled) {
        const cancel = document.createElement('button');
        cancel.type = 'button';
        cancel.className = 'sc-cancel';
        cancel.dataset.id = letter.newsletterId;
        cancel.textContent = '예약 취소';
        li.append(cancel);
      }

      listEl.append(li);
    });

    listStatus.hidden = letters.length > 0;
    if (!letters.length) listStatus.textContent = '예약할 뉴스레터가 없어요. 뉴스레터 제작에서 먼저 저장해 주세요.';
  }

  // ③ 불러오기
  async function loadSchedule() {
    try {
      const schedule = await api.getSchedule();
      const time = schedule.localTime;
      daySelect.value = schedule.dayOfMonth;
      timeInput.value = time;
      savedCycle = { day: schedule.dayOfMonth, time };
    } catch (error) {
      if (error.status === 404) { savedCycle = null; return; } // 아직 주기 설정이 없음
      handleError(error, '발행 주기 불러오기 실패');
    }
  }

  async function loadLetters() {
    listStatus.hidden = false;
    listStatus.textContent = '뉴스레터를 불러오는 중이에요…';
    try {
      const [saved, scheduled] = await Promise.all([api.getNewsletters('SAVED'), api.getNewsletters('SCHEDULED')]);
      letters = [...saved.newsletters, ...scheduled.newsletters].sort((a, b) =>
        Date.parse(b.createdAt) - Date.parse(a.createdAt) || b.newsletterId.localeCompare(a.newsletterId)
      );
      render();
    } catch (error) {
      listStatus.textContent = '뉴스레터를 불러오지 못했어요.';
      handleError(error, '뉴스레터 불러오기 실패');
    }
  }

  function selectedTime() {
    return timeInput.value.length === 5 ? `${timeInput.value}:00` : timeInput.value;
  }

  async function saveCycle(day, time) {
    const schedule = await api.saveSchedule({ dayOfMonth: day, localTime: time });
    savedCycle = { day: schedule.dayOfMonth, time: schedule.localTime };
  }

  saveCycleBtn.addEventListener('click', async () => {
    if (busy) return;
    const time = selectedTime();
    if (!time) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '발행 시간을 선택해 주세요.' });
      return;
    }
    busy = true;
    saveCycleBtn.disabled = true;
    try {
      await saveCycle(Number(daySelect.value), time);
      DaroriAdmin.showDialog({ title: '저장 완료', description: '새 발행 주기는 이후 예약부터 적용돼요.' });
    } catch (error) {
      handleError(error, '발행 주기 저장 실패');
    } finally {
      busy = false;
      saveCycleBtn.disabled = false;
    }
  });

  // ④ 예약 (주기를 바꿨으면 주기 먼저 저장 → 성공하면 예약)
  async function reserve(letter, day, time, cycleChanged) {
    busy = true;
    submitBtn.disabled = true;
    try {
      if (cycleChanged) {
        await saveCycle(day, time);
      }
      const result = await api.reserve(letter.newsletterId);
      reservedAt[letter.newsletterId] = result.scheduledAt;
      await loadLetters();
      DaroriAdmin.showDialog({ title: '예약 완료', description: `${formatDateTime(result.scheduledAt)}에 발행될 예정이에요.` });
    } catch (error) {
      handleError(error, '예약 실패'); // 같은 시간에 다른 예약이 있으면(409) 서버 메시지 표시
    } finally {
      busy = false;
      submitBtn.disabled = false;
    }
  }

  form.addEventListener('submit', e => {
    e.preventDefault(); // 페이지 새로고침 막기
    if (busy) return;

    const checked = listEl.querySelector('input:checked');
    if (!checked) {
      DaroriAdmin.showDialog({ title: '뉴스레터 선택', description: '발행할 뉴스레터를 하나 선택해 주세요.' });
      return;
    }
    if (!timeInput.value) {
      DaroriAdmin.showDialog({ title: '입력 확인', description: '발행 시간을 선택해 주세요.' });
      return;
    }

    const letter = letters.find(l => l.newsletterId === checked.value);
    const day = Number(daySelect.value);
    const time = selectedTime();
    const cycleText = `매월 ${day}일 ${formatTime(time)}`;
    const cycleChanged = !savedCycle || savedCycle.day !== day || savedCycle.time !== time;

    DaroriAdmin.showDialog({
      title: '발행 예약',
      description: cycleChanged
        ? `발행 주기를 '${cycleText}'(으)로 바꾸고, '${letter.title}'을(를) 예약할까요? 바뀐 주기는 앞으로의 예약에 적용돼요.`
        : `'${letter.title}'을(를) ${cycleText} 주기에 맞춰 예약할까요?`,
      confirmLabel: '예약하기',
      onConfirm: () => reserve(letter, day, time, cycleChanged)
    });
  });

  // ⑤ 예약 취소
  async function cancelReservation(letter) {
    busy = true;
    try {
      await api.cancelReservation(letter.newsletterId);
      delete reservedAt[letter.newsletterId];
      await loadLetters();
      DaroriAdmin.showDialog({ title: '예약 취소 완료', description: `'${letter.title}'의 발행 예약을 취소했어요.` });
    } catch (error) {
      handleError(error, '예약 취소 실패'); // 이미 발송이 시작된 경우(409) 등
    } finally {
      busy = false;
    }
  }

  async function openPreview(id) {
    const revision = ++previewRevision;
    previewStatus.textContent = '저장한 편지를 불러오는 중이에요…';
    previewFrame.hidden = true;
    previewFrame.removeAttribute('srcdoc');
    previewDialog.showModal();
    try {
      const draft = await api.getNewsletter(id);
      if (!previewDialog.open || revision !== previewRevision) return;
      const date = draft.publishedAt || draft.scheduledAt;
      const issue = date ? new Intl.DateTimeFormat('ko-KR', {
        timeZone: 'Asia/Seoul', year: 'numeric', month: 'long'
      }).format(new Date(date)) + '호' : null;
      const stories = draft.stories.map(story => ({
        ...story,
        images: story.images.map(image => ({ ...image, imageUrl: image.imageUrl ?? image.url }))
      }));
      const quiz = draft.quiz && { ...draft.quiz, options: draft.quiz.options ?? draft.quiz.choices };
      const { html } = await api.previewNewsletter({ ...draft, issue, stories, quiz });
      if (!previewDialog.open || revision !== previewRevision) return;
      previewFrame.srcdoc = html;
      previewFrame.hidden = false;
      previewStatus.textContent = '';
    } catch (error) {
      if (!previewDialog.open || revision !== previewRevision) return;
      if (error.kind === 'session') handleError(error);
      else previewStatus.textContent = error.message || '미리보기를 불러오지 못했어요.';
    }
  }

  previewDialog.addEventListener('close', () => {
    ++previewRevision;
    previewFrame.removeAttribute('srcdoc');
  });

  listEl.addEventListener('click', e => {
    const preview = e.target.closest('.sc-preview');
    if (preview) {
      openPreview(preview.dataset.id);
      return;
    }
    const btn = e.target.closest('.sc-cancel');
    if (!btn || busy) return;
    const letter = letters.find(l => l.newsletterId === btn.dataset.id);

    DaroriAdmin.showDialog({
      title: '예약 취소',
      description: `'${letter.title}'의 발행 예약을 취소할까요?`,
      confirmLabel: '예약 취소',
      onConfirm: () => cancelReservation(letter)
    });
  });

  // ── 시작: 로그인 확인이 끝난 뒤 ──
  window.DaroriAdminAuthPage.ready.then(account => {
    if (account) Promise.all([loadSchedule(), loadLetters()]);
  });
})();