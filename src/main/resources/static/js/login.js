const AUTH_TOKEN_KEY = 'authToken';
const AUTH_USER_KEY = 'authUser';

document.addEventListener('DOMContentLoaded', () => {
    if (sessionStorage.getItem(AUTH_TOKEN_KEY)) {
        window.location.href = '/app.html';
        return;
    }

    document.getElementById('form-login').addEventListener('submit', handleLogin);
});

async function handleLogin(e) {
    e.preventDefault();

    const user = document.getElementById('login-user').value.trim();
    const pass = document.getElementById('login-pass').value;
    const errorEl = document.getElementById('login-error');
    const submitBtn = document.getElementById('btn-login');

    errorEl.classList.add('hidden');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Validando...';

    const token = btoa(`${user}:${pass}`);

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });

        if (response.status === 401) {
            const data = await response.json().catch(() => ({}));
            showLoginError(data.message || 'Usuário ou senha inválidos.');
            return;
        }

        if (!response.ok) {
            showLoginError('Não foi possível validar as credenciais. Tente novamente.');
            return;
        }

        sessionStorage.setItem(AUTH_TOKEN_KEY, token);
        sessionStorage.setItem(AUTH_USER_KEY, user);
        window.location.href = '/app.html';
    } catch (_) {
        showLoginError('Erro de conexão com o servidor.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Entrar';
    }
}

function showLoginError(message) {
    const errorEl = document.getElementById('login-error');
    errorEl.textContent = message;
    errorEl.classList.remove('hidden');
}
