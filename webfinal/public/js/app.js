$(function () {
  const api = '/items';
  const logsApi = '/logs';
  let editingId = null;
  const $formTitle = $('#form-title');
  const $status = $('#status');
  const $form = $('#item-form');
  const $cancel = $('#cancel-btn');
  const $save = $('#save-btn');
  const $tableBody = $('#items-table tbody');
  const $logsBody = $('#logs-table tbody');

  const setStatus = (message, isError = false) => {
    $status.text(message || '').css('color', isError ? '#dc2626' : '#16a34a');
  };

  const resetForm = () => {
    editingId = null;
    $('#item-id').val('');
    $('#title').val('');
    $('#description').val('');
    $('#amount').val('');
    $formTitle.text('Create Entry');
    $save.text('Save');
    $cancel.attr('hidden', true);
  };

  const renderRows = (items) => {
    $tableBody.empty();
    if (!items.length) {
      $tableBody.append('<tr><td colspan="5">No entries yet.</td></tr>');
      return;
    }
    items.forEach((item) => {
      const tr = $('<tr></tr>');
      tr.append(`<td>${item.title}</td>`);
      tr.append(`<td>${item.description || ''}</td>`);
      tr.append(`<td><span class="badge">$${Number(item.amount || 0).toFixed(2)}</span></td>`);
      tr.append(`<td>${item.updatedAt ? new Date(item.updatedAt).toLocaleString() : ''}</td>`);
      tr.append(
        `<td class="actions-cell">
          <button type="button" class="ghost edit-btn" data-id="${item.id}">Edit</button>
          <button type="button" class="ghost delete-btn" data-id="${item.id}">Delete</button>
        </td>`
      );
      tr.data('item', item);
      $tableBody.append(tr);
    });
  };

  const loadItems = () => {
    setStatus('Loading...');
    $.get(api)
      .done((items) => {
        renderRows(items);
        setStatus('Loaded');
      })
      .fail((xhr) => {
        setStatus(xhr.responseJSON?.error || 'Failed to load items', true);
      });
  };

  $form.on('submit', function (evt) {
    evt.preventDefault();
    const payload = {
      title: $('#title').val().trim(),
      description: $('#description').val().trim(),
      amount: $('#amount').val()
    };

    if (!payload.title) {
      setStatus('Title is required', true);
      return;
    }

    const method = editingId ? 'PUT' : 'POST';
    const url = editingId ? `${api}/${editingId}` : api;

    $.ajax({
      url,
      method,
      contentType: 'application/json',
      data: JSON.stringify(payload)
    })
      .done(() => {
        resetForm();
        loadItems();
        loadLogs();
        setStatus('Saved');
      })
      .fail((xhr) => {
        const message = xhr.responseJSON?.error || 'Request failed';
        setStatus(message, true);
      });
  });

  $cancel.on('click', () => {
    resetForm();
    setStatus('Edit cancelled');
  });

  $('#refresh-btn').on('click', loadItems);

  $tableBody.on('click', '.edit-btn', function () {
    const item = $(this).closest('tr').data('item');
    if (!item) return;
    editingId = item.id;
    $('#item-id').val(item.id);
    $('#title').val(item.title);
    $('#description').val(item.description);
    $('#amount').val(item.amount);
    $formTitle.text('Edit Entry');
    $save.text('Update');
    $cancel.removeAttr('hidden');
    setStatus(`Editing #${item.id}`);
  });

  $tableBody.on('click', '.delete-btn', function () {
    const item = $(this).closest('tr').data('item');
    if (!item) return;
    if (!window.confirm('Delete this entry?')) return;
    $.ajax({ url: `${api}/${item.id}`, method: 'DELETE' })
      .done(() => {
        if (editingId === item.id) resetForm();
        loadItems();
        loadLogs();
        setStatus('Deleted');
      })
      .fail((xhr) => {
        const message = xhr.responseJSON?.error || 'Delete failed';
        setStatus(message, true);
      });
  });

  // ---- Logs ----
  const actionColors = {
    CREATE: '#16a34a',
    UPDATE: '#2563eb',
    DELETE: '#dc2626'
  };

  const renderLogs = (logs) => {
    $logsBody.empty();
    if (!logs.length) {
      $logsBody.append('<tr><td colspan="4">No logs yet.</td></tr>');
      return;
    }
    logs.forEach((log) => {
      const tr = $('<tr></tr>');
      tr.append(`<td><span style="color:${actionColors[log.action] || '#000'};font-weight:600">${log.action}</span></td>`);
      tr.append(`<td>${log.itemTitle || ''}</td>`);
      tr.append(`<td>${log.details || ''}</td>`);
      tr.append(`<td>${log.timestamp ? new Date(log.timestamp).toLocaleString() : ''}</td>`);
      $logsBody.append(tr);
    });
  };

  const loadLogs = () => {
    $.get(logsApi)
      .done((logs) => {
        renderLogs(logs);
      })
      .fail(() => {
        $logsBody.html('<tr><td colspan="4">Failed to load logs</td></tr>');
      });
  };

  $('#refresh-logs-btn').on('click', loadLogs);

  $('#clear-logs-btn').on('click', () => {
    if (!window.confirm('Clear all logs?')) return;
    $.ajax({ url: logsApi, method: 'DELETE' })
      .done(() => {
        loadLogs();
        setStatus('Logs cleared');
      })
      .fail(() => {
        setStatus('Failed to clear logs', true);
      });
  });

  loadItems();
  loadLogs();
});
