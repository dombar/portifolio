const syncedMembers = [];
let selectedProjectId = null;

const AUTH_TOKEN_KEY = 'authToken';
const AUTH_USER_KEY = 'authUser';

const PROJECT_STATUSES = [
    'EM_ANALISE', 'ANALISE_REALIZADA', 'ANALISE_APROVADA',
    'INICIADO', 'PLANEJADO', 'EM_ANDAMENTO', 'ENCERRADO', 'CANCELADO'
];

document.addEventListener('DOMContentLoaded', () => {
    if (!requireAuth()) return;

    const user = sessionStorage.getItem(AUTH_USER_KEY);
    if (user) {
        document.getElementById('logged-user').textContent = user;
    }

    document.getElementById('btn-logout').addEventListener('click', logout);
    initTabs();
    initEventListeners();
});

function requireAuth() {
    if (!sessionStorage.getItem(AUTH_TOKEN_KEY)) {
        window.location.href = '/';
        return false;
    }
    return true;
}

function logout() {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    sessionStorage.removeItem(AUTH_USER_KEY);
    window.location.href = '/';
}

function getAuthHeader() {
    const token = sessionStorage.getItem(AUTH_TOKEN_KEY);
    return token ? 'Basic ' + token : '';
}

function initTabs() {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
            document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
            tab.classList.add('active');
            document.getElementById(`tab-${tab.dataset.tab}`).classList.add('active');
        });
    });
}

function initEventListeners() {
    document.getElementById('btn-refresh-report').addEventListener('click', renderReport);
    document.getElementById('btn-refresh-projects').addEventListener('click', renderProjects);
    document.getElementById('btn-refresh-members').addEventListener('click', renderExternalMembers);
    document.getElementById('btn-close-detail').addEventListener('click', closeProjectDetail);

    document.getElementById('form-create-project').addEventListener('submit', handleCreateProject);
    document.getElementById('form-create-member').addEventListener('submit', handleCreateMember);
}

async function apiFetch(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        'Authorization': getAuthHeader(),
        ...(options.headers || {})
    };

    const response = await fetch(path, { ...options, headers });

    if (response.status === 401) {
        logout();
        return;
    }

    if (!response.ok) {
        let message = `Erro ${response.status}`;
        try {
            const err = await response.json();
            message = err.message || err.error || message;
        } catch (_) { /* ignore */ }
        throw new Error(message);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

async function externalFetch(path, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    const response = await fetch(path, { ...options, headers });

    if (!response.ok) {
        let message = `Erro ${response.status}`;
        try {
            const err = await response.json();
            message = err.message || err.error || message;
        } catch (_) { /* ignore */ }
        throw new Error(message);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function showMessage(text, type = 'error') {
    const banner = document.getElementById('message-banner');
    banner.textContent = text;
    banner.className = `message-banner ${type}`;
    setTimeout(() => banner.classList.add('hidden'), 5000);
}

function hideMessage() {
    document.getElementById('message-banner').classList.add('hidden');
}

function formatCurrency(value) {
    if (value == null) return '-';
    return Number(value).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

function formatDate(value) {
    if (!value) return '-';
    return new Date(value + 'T00:00:00').toLocaleDateString('pt-BR');
}

function statusLabel(status) {
    return status.replace(/_/g, ' ').toLowerCase();
}

function riskBadgeClass(risk) {
    if (!risk) return 'badge';
    return `badge badge-risk-${risk.toLowerCase()}`;
}

function updateGerenteSelect() {
    const select = document.getElementById('select-gerente');
    const managers = syncedMembers.filter(m => m.atribuicao === 'GERENTE');

    select.innerHTML = '<option value="">Selecione um membro sincronizado</option>';
    managers.forEach(m => {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = `${m.nome} (ID ${m.id})`;
        select.appendChild(opt);
    });
}

function renderSyncedMembersTable() {
    const container = document.getElementById('synced-members-container');

    if (syncedMembers.length === 0) {
        container.innerHTML = '<p class="placeholder">Nenhum membro sincronizado nesta sessão.</p>';
        return;
    }

    container.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>ID interno</th>
                    <th>ID externo</th>
                    <th>Nome</th>
                    <th>Atribuição</th>
                </tr>
            </thead>
            <tbody>
                ${syncedMembers.map(m => `
                    <tr>
                        <td>${m.id}</td>
                        <td>${m.externalId ?? '-'}</td>
                        <td>${escapeHtml(m.nome)}</td>
                        <td><span class="badge badge-role">${m.atribuicao}</span></td>
                    </tr>
                `).join('')}
            </tbody>
        </table>
    `;

    updateGerenteSelect();
}

async function renderReport() {
    hideMessage();
    const container = document.getElementById('report-content');

    try {
        const data = await apiFetch('/api/reports/portfolio');
        const qtyEntries = Object.entries(data.quantidadePorStatus || {});
        const budgetEntries = Object.entries(data.totalOrcadoPorStatus || {});

        container.innerHTML = `
            <div class="stat-card">
                <h4>Quantidade por status</h4>
                <ul>
                    ${qtyEntries.map(([s, v]) => `<li><span>${statusLabel(s)}</span><strong>${v}</strong></li>`).join('')}
                </ul>
            </div>
            <div class="stat-card">
                <h4>Total orçado por status</h4>
                <ul>
                    ${budgetEntries.map(([s, v]) => `<li><span>${statusLabel(s)}</span><strong>${formatCurrency(v)}</strong></li>`).join('')}
                </ul>
            </div>
            <div class="stat-card">
                <h4>Média duração (encerrados)</h4>
                <div class="value">${data.mediaDuracaoEncerrados != null ? data.mediaDuracaoEncerrados.toFixed(1) + ' dias' : '-'}</div>
            </div>
            <div class="stat-card">
                <h4>Membros únicos alocados</h4>
                <div class="value">${data.totalMembrosUnicosAlocados ?? 0}</div>
            </div>
        `;
        showMessage('Relatório atualizado com sucesso.', 'success');
    } catch (err) {
        container.innerHTML = '<p class="placeholder">Erro ao carregar relatório.</p>';
        showMessage(err.message);
    }
}

async function renderProjects() {
    hideMessage();
    const container = document.getElementById('projects-table-container');

    try {
        const data = await apiFetch('/api/projects?size=50');
        const projects = data.content || [];

        if (projects.length === 0) {
            container.innerHTML = '<p class="placeholder">Nenhum projeto encontrado.</p>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nome</th>
                        <th>Status</th>
                        <th>Risco</th>
                        <th>Gerente</th>
                        <th>Orçamento</th>
                    </tr>
                </thead>
                <tbody>
                    ${projects.map(p => `
                        <tr class="clickable" data-project-id="${p.id}">
                            <td>${p.id}</td>
                            <td>${escapeHtml(p.nome)}</td>
                            <td><span class="badge badge-status">${statusLabel(p.status)}</span></td>
                            <td><span class="${riskBadgeClass(p.nivelRisco)}">${p.nivelRisco || '-'}</span></td>
                            <td>${escapeHtml(p.gerenteNome || '-')}</td>
                            <td>${formatCurrency(p.orcamentoTotal)}</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

        container.querySelectorAll('tr.clickable').forEach(row => {
            row.addEventListener('click', () => openProjectDetail(Number(row.dataset.projectId)));
        });
    } catch (err) {
        container.innerHTML = '<p class="placeholder">Erro ao carregar projetos.</p>';
        showMessage(err.message);
    }
}

async function openProjectDetail(projectId) {
    hideMessage();
    selectedProjectId = projectId;
    const panel = document.getElementById('project-detail');
    const content = document.getElementById('project-detail-content');

    panel.classList.remove('hidden');
    content.innerHTML = '<p class="placeholder">Carregando...</p>';

    try {
        const project = await apiFetch(`/api/projects/${projectId}`);
        const members = await apiFetch(`/api/projects/${projectId}/members`) || [];
        const employees = syncedMembers.filter(m => m.atribuicao === 'FUNCIONARIO');

        content.innerHTML = `
            <div class="detail-grid">
                <div class="detail-item"><strong>Nome</strong>${escapeHtml(project.nome)}</div>
                <div class="detail-item"><strong>Status</strong><span class="badge badge-status">${statusLabel(project.status)}</span></div>
                <div class="detail-item"><strong>Risco</strong><span class="${riskBadgeClass(project.nivelRisco)}">${project.nivelRisco || '-'}</span></div>
                <div class="detail-item"><strong>Início</strong>${formatDate(project.dataInicio)}</div>
                <div class="detail-item"><strong>Previsão término</strong>${formatDate(project.previsaoTermino)}</div>
                <div class="detail-item"><strong>Término real</strong>${formatDate(project.dataRealTermino)}</div>
                <div class="detail-item"><strong>Orçamento</strong>${formatCurrency(project.orcamentoTotal)}</div>
                <div class="detail-item"><strong>Gerente</strong>${escapeHtml(project.gerenteNome || '-')} (ID ${project.gerenteId})</div>
                <div class="detail-item"><strong>Descrição</strong>${escapeHtml(project.descricao || '-')}</div>
            </div>

            <div class="detail-actions">
                <label>
                    Atualizar status
                    <select id="select-new-status">
                        ${PROJECT_STATUSES.map(s => `<option value="${s}" ${s === project.status ? 'selected' : ''}>${statusLabel(s)}</option>`).join('')}
                    </select>
                </label>
                <button class="btn btn-primary btn-sm" id="btn-update-status">Aplicar status</button>
                <button class="btn btn-danger btn-sm" id="btn-delete-project">Excluir projeto</button>
            </div>

            <h4>Membros alocados</h4>
            ${members.length === 0
                ? '<p class="placeholder">Nenhum membro alocado.</p>'
                : `<table>
                    <thead><tr><th>ID</th><th>Nome</th><th>Atribuição</th><th></th></tr></thead>
                    <tbody>
                        ${members.map(m => `
                            <tr>
                                <td>${m.id}</td>
                                <td>${escapeHtml(m.nome)}</td>
                                <td><span class="badge badge-role">${m.atribuicao}</span></td>
                                <td><button class="btn btn-danger btn-sm btn-remove-member" data-member-id="${m.id}">Remover</button></td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>`
            }

            <div class="detail-actions">
                <label>
                    Alocar membro
                    <select id="select-allocate-member">
                        <option value="">Selecione um funcionário sincronizado</option>
                        ${employees.map(m => `<option value="${m.id}">${escapeHtml(m.nome)} (ID ${m.id})</option>`).join('')}
                    </select>
                </label>
                <button class="btn btn-primary btn-sm" id="btn-allocate-member">Alocar</button>
            </div>
        `;

        document.getElementById('btn-update-status').addEventListener('click', () => handleUpdateStatus(projectId));
        document.getElementById('btn-delete-project').addEventListener('click', () => handleDeleteProject(projectId));
        document.getElementById('btn-allocate-member').addEventListener('click', () => handleAllocateMember(projectId));

        content.querySelectorAll('.btn-remove-member').forEach(btn => {
            btn.addEventListener('click', () => handleRemoveMember(projectId, Number(btn.dataset.memberId)));
        });

        panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    } catch (err) {
        content.innerHTML = '<p class="placeholder">Erro ao carregar detalhe.</p>';
        showMessage(err.message);
    }
}

function closeProjectDetail() {
    selectedProjectId = null;
    document.getElementById('project-detail').classList.add('hidden');
}

async function handleCreateProject(e) {
    e.preventDefault();
    hideMessage();
    const form = e.target;
    const body = {
        nome: form.nome.value,
        dataInicio: form.dataInicio.value,
        previsaoTermino: form.previsaoTermino.value,
        orcamentoTotal: Number(form.orcamentoTotal.value),
        descricao: form.descricao.value || null,
        gerenteId: Number(form.gerenteId.value)
    };

    try {
        await apiFetch('/api/projects', { method: 'POST', body: JSON.stringify(body) });
        form.reset();
        showMessage('Projeto criado com sucesso.', 'success');
        await renderProjects();
    } catch (err) {
        showMessage(err.message);
    }
}

async function handleUpdateStatus(projectId) {
    hideMessage();
    const status = document.getElementById('select-new-status').value;

    try {
        await apiFetch(`/api/projects/${projectId}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ status })
        });
        showMessage('Status atualizado com sucesso.', 'success');
        await openProjectDetail(projectId);
        await renderProjects();
    } catch (err) {
        showMessage(err.message);
    }
}

async function handleDeleteProject(projectId) {
    if (!confirm('Deseja realmente excluir este projeto?')) return;
    hideMessage();

    try {
        await apiFetch(`/api/projects/${projectId}`, { method: 'DELETE' });
        closeProjectDetail();
        showMessage('Projeto excluído com sucesso.', 'success');
        await renderProjects();
    } catch (err) {
        showMessage(err.message);
    }
}

async function handleAllocateMember(projectId) {
    hideMessage();
    const memberId = Number(document.getElementById('select-allocate-member').value);
    if (!memberId) {
        showMessage('Selecione um membro para alocar.');
        return;
    }

    try {
        await apiFetch(`/api/projects/${projectId}/members`, {
            method: 'POST',
            body: JSON.stringify({ memberId })
        });
        showMessage('Membro alocado com sucesso.', 'success');
        await openProjectDetail(projectId);
    } catch (err) {
        showMessage(err.message);
    }
}

async function handleRemoveMember(projectId, memberId) {
    hideMessage();

    try {
        await apiFetch(`/api/projects/${projectId}/members/${memberId}`, { method: 'DELETE' });
        showMessage('Membro removido com sucesso.', 'success');
        await openProjectDetail(projectId);
    } catch (err) {
        showMessage(err.message);
    }
}

async function renderExternalMembers() {
    hideMessage();
    const container = document.getElementById('external-members-container');

    try {
        const members = await externalFetch('/api/external/members') || [];

        if (members.length === 0) {
            container.innerHTML = '<p class="placeholder">Nenhum membro externo encontrado.</p>';
            return;
        }

        container.innerHTML = `
            <table>
                <thead>
                    <tr>
                        <th>ID externo</th>
                        <th>Nome</th>
                        <th>Atribuição</th>
                        <th></th>
                    </tr>
                </thead>
                <tbody>
                    ${members.map(m => {
                        const synced = syncedMembers.find(s => s.externalId === m.id);
                        return `
                            <tr>
                                <td>${m.id}</td>
                                <td>${escapeHtml(m.nome)}</td>
                                <td><span class="badge badge-role">${m.atribuicao}</span></td>
                                <td>
                                    ${synced
                                        ? `<span class="badge badge-status">Sincronizado (ID ${synced.id})</span>`
                                        : `<button class="btn btn-primary btn-sm btn-sync" data-external-id="${m.id}">Sincronizar</button>`
                                    }
                                </td>
                            </tr>
                        `;
                    }).join('')}
                </tbody>
            </table>
        `;

        container.querySelectorAll('.btn-sync').forEach(btn => {
            btn.addEventListener('click', () => handleSyncMember(Number(btn.dataset.externalId)));
        });
    } catch (err) {
        container.innerHTML = '<p class="placeholder">Erro ao carregar membros externos.</p>';
        showMessage(err.message);
    }
}

async function handleCreateMember(e) {
    e.preventDefault();
    hideMessage();
    const form = e.target;
    const body = {
        nome: form.nome.value,
        atribuicao: form.atribuicao.value
    };

    try {
        await externalFetch('/api/external/members', { method: 'POST', body: JSON.stringify(body) });
        form.reset();
        showMessage('Membro externo criado com sucesso.', 'success');
        await renderExternalMembers();
    } catch (err) {
        showMessage(err.message);
    }
}

async function handleSyncMember(externalId) {
    hideMessage();

    try {
        const member = await apiFetch(`/api/members/sync/${externalId}`, { method: 'POST' });
        const existing = syncedMembers.findIndex(m => m.id === member.id);
        const entry = {
            id: member.id,
            externalId: member.externalId ?? externalId,
            nome: member.nome,
            atribuicao: member.atribuicao
        };

        if (existing >= 0) {
            syncedMembers[existing] = entry;
        } else {
            syncedMembers.push(entry);
        }

        renderSyncedMembersTable();
        showMessage(`Membro "${member.nome}" sincronizado (ID interno: ${member.id}).`, 'success');
        await renderExternalMembers();
    } catch (err) {
        showMessage(err.message);
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
