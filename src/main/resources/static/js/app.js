/**
 * Square Games Platform — Frontend Application Logic
 * Architecture Microservices : Square Games API (:8080) & User Service (:8081)
 */

// Configuration des APIs
const GAME_API = ''; // Même hôte et port (:8080)
const USER_API = 'http://localhost:8081';

// État applicatif global
const state = {
  token: localStorage.getItem('sg_token') || null,
  user: JSON.parse(localStorage.getItem('sg_user') || 'null'),
  lang: localStorage.getItem('sg_lang') || 'fr',
  catalog: [],
  selectedGameType: 'tictactoe',
  currentGame: null,
  pollTimer: null,
  heartbeatTimer: null
};

// Dictionnaire de traductions client (i18n)
const i18n = {
  fr: {
    heartbeat_off: 'Serveur hors ligne',
    login_success: 'Connexion réussie ! Bienvenue {name}',
    login_failed: 'Échec de connexion : identifiants invalides',
    reg_success: 'Compte créé avec succès ! Connectez-vous.',
    reg_failed: 'Erreur lors de la création du compte',
    logout_success: 'Déconnexion réussie',
    game_created: 'Partie lancée avec succès !',
    game_create_failed: 'Impossible de créer la partie',
    not_logged_in: 'Veuillez vous connecter pour effectuer cette action',
    not_your_turn: "Ce n'est pas votre tour de jouer ! (Sanction 403)",
    turn_yours: "C'est à votre tour de jouer ! 🎯",
    turn_opponent: "Au tour du joueur : {id} ⏳",
    turn_waiting: 'En attente de démarrage...',
    turn_finished: 'Partie terminée ! 🏆',
    move_error: 'Coup invalide ou refusé',
    confirm_delete_user: 'Supprimer définitivement cet utilisateur ?',
    user_deleted: 'Utilisateur supprimé',
    no_games: 'Aucune partie trouvée. Créez-en une !',
    anonymous: 'Non connecté',
    refresh_ok: 'État de la partie mis à jour',
    status_running: 'EN COURS',
    status_terminated: 'TERMINÉE'
  },
  en: {
    heartbeat_off: 'Server offline',
    login_success: 'Login successful! Welcome {name}',
    login_failed: 'Login failed: invalid credentials',
    reg_success: 'Account created successfully! Please log in.',
    reg_failed: 'Account creation failed',
    logout_success: 'Successfully logged out',
    game_created: 'Game created successfully!',
    game_create_failed: 'Could not create game',
    not_logged_in: 'Please log in to perform this action',
    not_your_turn: "It is not your turn to play! (403 Forbidden)",
    turn_yours: "It's your turn to play! 🎯",
    turn_opponent: "Player's turn: {id} ⏳",
    turn_waiting: 'Waiting to start...',
    turn_finished: 'Game over! 🏆',
    move_error: 'Invalid or forbidden move',
    confirm_delete_user: 'Permanently delete this user?',
    user_deleted: 'User deleted',
    no_games: 'No games found. Create one!',
    anonymous: 'Not logged in',
    refresh_ok: 'Game state updated',
    status_running: 'RUNNING',
    status_terminated: 'TERMINATED'
  }
};

function t(key, params = {}) {
  let text = (i18n[state.lang] && i18n[state.lang][key]) || i18n['fr'][key] || key;
  for (const [k, v] of Object.entries(params)) {
    text = text.replace(`{${k}}`, v);
  }
  return text;
}

// ==========================================
// TOAST NOTIFICATIONS
// ==========================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const icon = type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️';
  toast.innerHTML = `<span style="font-size:1.2rem;">${icon}</span><span>${message}</span>`;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// ==========================================
// HEADERS HTTP D'AUTHENTIFICATION
// ==========================================
function getAuthHeaders() {
  const headers = {
    'Content-Type': 'application/json',
    'Accept-Language': state.lang
  };
  if (state.token) {
    headers['Authorization'] = `Bearer ${state.token}`;
  }
  if (state.user && state.user.id) {
    headers['X-UserId'] = state.user.id;
  }
  return headers;
}

// ==========================================
// MONITORING HEARTBEAT
// ==========================================
async function checkHeartbeat() {
  const valElem = document.getElementById('heartbeat-val');
  const dot = document.querySelector('.pulse-dot');
  if (!valElem) return;

  try {
    const res = await fetch(`${GAME_API}/heartbeat`);
    if (res.ok) {
      const bpm = await res.text();
      valElem.textContent = `${bpm} bpm`;
      if (dot) dot.style.backgroundColor = 'var(--accent-success)';
    } else {
      valElem.textContent = '-- bpm';
      if (dot) dot.style.backgroundColor = 'var(--accent-danger)';
    }
  } catch (err) {
    valElem.textContent = t('heartbeat_off');
    if (dot) dot.style.backgroundColor = 'var(--accent-danger)';
  }
}

function startHeartbeatMonitor() {
  checkHeartbeat();
  if (state.heartbeatTimer) clearInterval(state.heartbeatTimer);
  state.heartbeatTimer = setInterval(checkHeartbeat, 5000);
}

// ==========================================
// GESTION DE LA LANGUE (i18n)
// ==========================================
function setLanguage(lang) {
  state.lang = lang;
  localStorage.setItem('sg_lang', lang);

  document.getElementById('btn-lang-fr')?.classList.toggle('active', lang === 'fr');
  document.getElementById('btn-lang-en')?.classList.toggle('active', lang === 'en');

  // Recharge le catalogue avec le nouvel en-tête Accept-Language
  loadCatalog();

  // Si une partie est ouverte, rafraîchir l'affichage
  if (state.currentGame) {
    renderGame(state.currentGame);
  }
}

// ==========================================
// AUTHENTIFICATION & UTILISATEURS
// ==========================================
function showAuthTab(tab) {
  const tabLogin = document.getElementById('tab-login');
  const tabReg = document.getElementById('tab-register');
  const formLogin = document.getElementById('form-login');
  const formReg = document.getElementById('form-register');

  if (tab === 'login') {
    tabLogin?.classList.add('active');
    tabReg?.classList.remove('active');
    if (formLogin) formLogin.style.display = 'block';
    if (formReg) formReg.style.display = 'none';
  } else {
    tabLogin?.classList.remove('active');
    tabReg?.classList.add('active');
    if (formLogin) formLogin.style.display = 'none';
    if (formReg) formReg.style.display = 'block';
  }
}

async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('login-username').value.trim();
  const password = document.getElementById('login-password').value;

  try {
    const res = await fetch(`${USER_API}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showToast(err.error || t('login_failed'), 'error');
      return;
    }

    const data = await res.json();
    state.token = data.token;
    state.user = {
      id: data.userId,
      username: data.username,
      role: data.role
    };

    localStorage.setItem('sg_token', state.token);
    localStorage.setItem('sg_user', JSON.stringify(state.user));

    showToast(t('login_success', { name: data.username }), 'success');
    updateAuthUI();
    loadUserGames();

    if (state.user.role === 'ROLE_ADMIN') {
      loadAdminUsers();
    }
  } catch (err) {
    showToast(t('login_failed') + ` (${err.message})`, 'error');
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const username = document.getElementById('reg-username').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value;
  const role = document.getElementById('reg-role').value;

  try {
    const res = await fetch(`${USER_API}/users`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, password, role })
    });

    if (!res.ok) {
      showToast(t('reg_failed'), 'error');
      return;
    }

    showToast(t('reg_success'), 'success');
    showAuthTab('login');
    document.getElementById('login-username').value = username;
    document.getElementById('login-password').value = password;
  } catch (err) {
    showToast(t('reg_failed') + ` (${err.message})`, 'error');
  }
}

function handleLogout() {
  state.token = null;
  state.user = null;
  state.currentGame = null;
  localStorage.removeItem('sg_token');
  localStorage.removeItem('sg_user');

  if (state.pollTimer) clearInterval(state.pollTimer);

  updateAuthUI();
  resetGameView();
  showToast(t('logout_success'), 'info');
}

function updateAuthUI() {
  const formLogin = document.getElementById('form-login');
  const formReg = document.getElementById('form-register');
  const authTabs = document.getElementById('auth-tabs');
  const loggedInView = document.getElementById('logged-in-view');
  const userNavArea = document.getElementById('user-nav-area');
  const userDisplayName = document.getElementById('user-display-name');
  const userDisplayId = document.getElementById('user-display-id');
  const adminCard = document.getElementById('admin-card');
  const historyList = document.getElementById('games-history-list');

  if (state.user && state.token) {
    if (formLogin) formLogin.style.display = 'none';
    if (formReg) formReg.style.display = 'none';
    if (authTabs) authTabs.style.display = 'none';
    if (loggedInView) loggedInView.style.display = 'block';

    if (userDisplayName) userDisplayName.textContent = state.user.username;
    if (userDisplayId) userDisplayId.textContent = state.user.id;

    const roleBadgeClass = state.user.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user';
    const roleLabel = state.user.role === 'ROLE_ADMIN' ? 'ADMIN' : 'PLAYER';

    if (userNavArea) {
      userNavArea.innerHTML = `
        <div class="user-profile">
          <span>👤 ${state.user.username}</span>
          <span class="user-badge ${roleBadgeClass}">${roleLabel}</span>
        </div>
      `;
    }

    if (adminCard) {
      adminCard.style.display = state.user.role === 'ROLE_ADMIN' ? 'block' : 'none';
    }
  } else {
    showAuthTab('login');
    if (authTabs) authTabs.style.display = 'flex';
    if (loggedInView) loggedInView.style.display = 'none';

    if (userNavArea) {
      userNavArea.innerHTML = `<span style="font-size:0.85rem; color:var(--text-muted);">${t('anonymous')}</span>`;
    }

    if (adminCard) adminCard.style.display = 'none';
    if (historyList) {
      historyList.innerHTML = `<p style="font-size: 0.85rem; color: var(--text-muted);">${t('not_logged_in')}</p>`;
    }
  }
}

// ==========================================
// CATALOGUE & CRÉATION DE JEUX
// ==========================================
const GAME_ICONS = {
  'tictactoe': '❌⭕',
  '15 puzzle': '🧩',
  'taquin': '🧩',
  'connectfour': '🔴🟡',
  'connect4': '🔴🟡'
};

async function loadCatalog() {
  const container = document.getElementById('catalog-container');
  if (!container) return;

  try {
    const res = await fetch(`${GAME_API}/api/catalog/games`, {
      headers: { 'Accept-Language': state.lang }
    });

    if (!res.ok) return;

    const games = await res.json();
    state.catalog = games;
    container.innerHTML = '';

    games.forEach((game, idx) => {
      const card = document.createElement('div');
      const isSelected = (game.id === state.selectedGameType) || (!state.selectedGameType && idx === 0);
      if (isSelected) state.selectedGameType = game.id;

      card.className = `game-card-select ${isSelected ? 'selected' : ''}`;
      card.id = `catalog-game-${game.id}`;
      card.onclick = () => selectCatalogGame(game);

      const icon = GAME_ICONS[game.id.toLowerCase()] || '🎲';
      card.innerHTML = `
        <div class="game-card-icon">${icon}</div>
        <div class="game-card-title">${game.name}</div>
        <div style="font-size: 0.7rem; color: var(--text-muted); margin-top: 0.25rem;">
          ${game.defaultBoardSize}x${game.defaultBoardSize} | ${game.defaultPlayerCount}J
        </div>
      `;
      container.appendChild(card);
    });

    // Mettre à jour les champs de saisie du formulaire de création
    const selected = games.find(g => g.id === state.selectedGameType) || games[0];
    if (selected) {
      document.getElementById('input-board-size').value = selected.defaultBoardSize;
      document.getElementById('input-player-count').value = selected.defaultPlayerCount;
      document.getElementById('selected-game-type').value = selected.id;
    }
  } catch (err) {
    console.error('Erreur chargement catalogue :', err);
  }
}

function selectCatalogGame(game) {
  state.selectedGameType = game.id;
  document.querySelectorAll('.game-card-select').forEach(el => el.classList.remove('selected'));
  document.getElementById(`catalog-game-${game.id}`)?.classList.add('selected');

  document.getElementById('selected-game-type').value = game.id;
  const boardInput = document.getElementById('input-board-size');
  const playerInput = document.getElementById('input-player-count');

  boardInput.value = game.defaultBoardSize;
  playerInput.value = game.defaultPlayerCount;

  const gid = (game.id || '').toLowerCase();
  if (gid.includes('connect')) {
    boardInput.disabled = true;
    playerInput.disabled = true;
  } else if (gid.includes('puzzle') || gid.includes('taquin')) {
    boardInput.disabled = false;
    boardInput.min = 3;
    boardInput.max = 8;
    playerInput.disabled = true;
  } else {
    boardInput.disabled = false;
    boardInput.min = 3;
    boardInput.max = 8;
    playerInput.disabled = false;
    playerInput.min = 1;
    playerInput.max = 4;
  }
}

async function handleCreateGame(e) {
  e.preventDefault();
  if (!state.token || !state.user) {
    showToast(t('not_logged_in'), 'error');
    return;
  }

  const gameType = document.getElementById('selected-game-type').value;
  let boardSize = parseInt(document.getElementById('input-board-size').value, 10);
  let playerCount = parseInt(document.getElementById('input-player-count').value, 10);

  const gid = (gameType || '').toLowerCase();
  if (gid.includes('connect')) {
    boardSize = 7;
    playerCount = 2;
  } else if (gid.includes('puzzle') || gid.includes('taquin')) {
    playerCount = 1;
  }

  try {
    const res = await fetch(`${GAME_API}/games`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ gameType, boardSize, playerCount })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showToast(err.error || t('game_create_failed'), 'error');
      return;
    }

    const newGame = await res.json();
    showToast(t('game_created'), 'success');
    await loadGame(newGame.id);
    loadUserGames();
  } catch (err) {
    showToast(t('game_create_failed') + ` (${err.message})`, 'error');
  }
}

// ==========================================
// ARÈNE DE JEU & GESTION DES COUPS
// ==========================================
async function loadGame(gameId) {
  if (!gameId) return;

  try {
    const res = await fetch(`${GAME_API}/games/${gameId}`, {
      headers: getAuthHeaders()
    });

    if (!res.ok) {
      showToast(t('move_error'), 'error');
      return;
    }

    const game = await res.json();
    state.currentGame = game;
    renderGame(game);

    // Polling régulier pour mettre à jour les coups de l'adversaire
    if (state.pollTimer) clearInterval(state.pollTimer);
    if (game.status === 'ONGOING' || game.status === 'RUNNING') {
      state.pollTimer = setInterval(async () => {
        if (!state.currentGame || (state.currentGame.status !== 'ONGOING' && state.currentGame.status !== 'RUNNING')) {
          clearInterval(state.pollTimer);
          return;
        }
        const pollRes = await fetch(`${GAME_API}/games/${state.currentGame.id}`, {
          headers: getAuthHeaders()
        });
        if (pollRes.ok) {
          const updated = await pollRes.json();
          // Re-render uniquement si l'état ou le joueur courant a changé
          if (JSON.stringify(updated.board) !== JSON.stringify(state.currentGame.board)
              || updated.currentPlayerId !== state.currentGame.currentPlayerId
              || updated.status !== state.currentGame.status) {
            state.currentGame = updated;
            renderGame(updated);
          }
        }
      }, 3000);
    }
  } catch (err) {
    console.error('Erreur chargement partie :', err);
  }
}

function parseBoardKey(key) {
  // Supporte : CellPosition[x=0, y=1], CellPosition[x:0, y:1], "0,1", "x=0,y=1"
  const match = key.match(/x\s*[:=]\s*(\d+).*?y\s*[:=]\s*(\d+)/i) || key.match(/(\d+)\s*,\s*(\d+)/);
  if (match) {
    return { x: parseInt(match[1], 10), y: parseInt(match[2], 10) };
  }
  return null;
}

function renderGame(game) {
  const title = document.getElementById('active-game-title');
  const subtitle = document.getElementById('active-game-subtitle');
  const badges = document.getElementById('active-game-badges');
  const turnBanner = document.getElementById('turn-banner');
  const turnIndicator = document.getElementById('turn-indicator');
  const turnIcon = document.getElementById('turn-icon');
  const turnText = document.getElementById('turn-text');
  const boardGrid = document.getElementById('board-grid');
  const placeholder = document.getElementById('board-placeholder');
  const controls = document.getElementById('game-controls');

  if (placeholder) placeholder.style.display = 'none';
  if (boardGrid) boardGrid.style.display = 'grid';
  if (controls) controls.style.display = 'flex';
  if (turnIndicator) turnIndicator.style.display = 'block';

  // Titre & Sous-titre
  const catItem = state.catalog.find(g => g.id.toLowerCase() === game.factoryId?.toLowerCase());
  const gameName = catItem ? catItem.name : (game.factoryId || 'Square Game');
  if (title) title.textContent = gameName;
  if (subtitle) subtitle.textContent = `ID : ${game.id} | Grille : ${game.boardSize}x${game.boardSize}`;

  // Badges de statut
  const isRunning = game.status === 'ONGOING' || game.status === 'RUNNING';
  const statusLabel = isRunning ? t('status_running') : t('status_terminated');
  const statusBadgeColor = isRunning ? 'var(--accent-success)' : 'var(--accent-danger)';

  if (badges) {
    badges.innerHTML = `
      <span style="background: rgba(255,255,255,0.08); padding: 0.25rem 0.6rem; border-radius: 4px; font-size: 0.75rem;">
        🎮 ${game.playerIds ? game.playerIds.length : 1} Joueur(s)
      </span>
      <span style="background: ${statusBadgeColor}22; color: ${statusBadgeColor}; border: 1px solid ${statusBadgeColor}; padding: 0.25rem 0.6rem; border-radius: 4px; font-size: 0.75rem; font-weight: 700;">
        ${statusLabel}
      </span>
    `;
  }

  // Gestion du tour de rôle
  const myUserId = state.user?.id;
  const isMyTurn = isRunning && game.currentPlayerId && (game.currentPlayerId.toString() === myUserId);

  if (turnBanner) {
    turnBanner.className = 'turn-banner';
    if (!isRunning) {
      turnBanner.classList.add('turn-finished');
      if (turnIcon) turnIcon.textContent = '🏆';
      if (turnText) turnText.textContent = t('turn_finished');
    } else if (isMyTurn) {
      turnBanner.classList.add('my-turn');
      if (turnIcon) turnIcon.textContent = '🎯';
      if (turnText) turnText.textContent = t('turn_yours');
    } else {
      const oppId = game.currentPlayerId ? (game.currentPlayerId.toString().substring(0, 8) + '...') : '--';
      if (turnIcon) turnIcon.textContent = '⏳';
      if (turnText) turnText.textContent = t('turn_opponent', { id: oppId });
    }
  }

  // Spécificités par type de jeu
  const factoryIdLower = (game.factoryId || '').toLowerCase();
  const isConnectFour = factoryIdLower.includes('connect');
  const isTaquin = factoryIdLower.includes('puzzle') || factoryIdLower.includes('taquin');

  // Rendu de la grille
  const cols = isConnectFour ? 7 : (game.boardSize || 3);
  const rows = isConnectFour ? 6 : (game.boardSize || 3);
  const cellSize = Math.max(46, Math.min(84, Math.floor(380 / cols)));

  boardGrid.style.gridTemplateColumns = `repeat(${cols}, ${cellSize}px)`;
  boardGrid.innerHTML = '';

  // Indexation des jetons sur le plateau
  const boardTokens = {};
  if (game.board) {
    for (const [key, token] of Object.entries(game.board)) {
      const pos = parseBoardKey(key);
      if (pos) {
        boardTokens[`${pos.x},${pos.y}`] = token;
      }
    }
  }

  // Pour Puissance 4 : y va de 5 à 0 afin que la base (y=0) soit visuellement en bas du plateau
  const yValues = [];
  if (isConnectFour) {
    for (let y = rows - 1; y >= 0; y--) yValues.push(y);
  } else {
    for (let y = 0; y < rows; y++) yValues.push(y);
  }

  // Création des cases
  for (const y of yValues) {
    for (let x = 0; x < cols; x++) {
      const cell = document.createElement('div');
      cell.className = 'cell';
      cell.style.width = `${cellSize}px`;
      cell.style.height = `${cellSize}px`;

      const token = boardTokens[`${x},${y}`];

      if (token) {
        cell.classList.add('occupied');
        const tokenName = token.name || '';
        let tokenClass = '';
        let displayContent = tokenName;

        if (tokenName.toUpperCase() === 'X') {
          tokenClass = 'token-x';
          displayContent = 'X';
        } else if (tokenName.toUpperCase() === 'O' || tokenName === '0') {
          tokenClass = 'token-o';
          displayContent = 'O';
        } else if (tokenName.toUpperCase() === 'R' || tokenName.toLowerCase().includes('red') || tokenName.toLowerCase().includes('rouge')) {
          tokenClass = 'token-red';
          displayContent = '🔴';
        } else if (tokenName.toUpperCase() === 'Y' || tokenName.toLowerCase().includes('yellow') || tokenName.toLowerCase().includes('jaune')) {
          tokenClass = 'token-yellow';
          displayContent = '🟡';
        } else if (!isNaN(tokenName)) {
          tokenClass = 'token-num';
          displayContent = tokenName;
        }

        cell.innerHTML = `<span class="${tokenClass}">${displayContent}</span>`;

        // Taquin : seules les tuiles adjacentes à la case vide peuvent glisser
        if (isTaquin) {
          const canMove = isRunning && token.allowedMoves && token.allowedMoves.length > 0;
          if (canMove) {
            cell.classList.add('movable', 'taquin-movable');
            cell.onclick = () => handleCellClick(x, y);
            cell.title = `Glisser la tuile ${tokenName} vers l'espace vide`;
          } else {
            cell.classList.add('taquin-static');
            cell.title = `Tuile ${tokenName}`;
          }
        }

        // Puissance 4 : cliquer sur une colonne dépose un jeton au sommet
        if (isConnectFour && isRunning) {
          cell.classList.add('movable');
          cell.onclick = () => handleCellClick(x, 0);
          cell.title = `Jouer colonne ${x + 1}`;
        }
      } else {
        // Case vide
        if (isTaquin) {
          cell.classList.add('taquin-empty');
          cell.innerHTML = '<span class="empty-slot-label">VIDE</span>';
          cell.title = 'Espace vide';
        } else if (isRunning) {
          cell.onclick = () => handleCellClick(x, y);
          cell.title = isConnectFour ? `Jouer colonne ${x + 1}` : `Jouer en (${x}, ${y})`;
        }
      }

      boardGrid.appendChild(cell);
    }
  }
}

async function handleCellClick(x, y) {
  if (!state.token || !state.user) {
    showToast(t('not_logged_in'), 'error');
    return;
  }

  if (!state.currentGame || (state.currentGame.status !== 'ONGOING' && state.currentGame.status !== 'RUNNING')) {
    return;
  }

  // Vérification de sécurité locale
  const isMyTurn = state.currentGame.currentPlayerId && (state.currentGame.currentPlayerId.toString() === state.user.id);
  if (!isMyTurn) {
    showToast(t('not_your_turn'), 'error');
    return;
  }

  try {
    const res = await fetch(`${GAME_API}/games/${state.currentGame.id}/moves`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({ x, y })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      showToast(err.error || t('move_error'), 'error');
      return;
    }

    const updated = await res.json();
    state.currentGame = updated;
    renderGame(updated);
    loadUserGames();

    if (updated.status === 'TERMINATED') {
      showToast(t('turn_finished'), 'success');
    }
  } catch (err) {
    showToast(t('move_error') + ` (${err.message})`, 'error');
  }
}

async function refreshCurrentGame() {
  if (!state.currentGame) return;
  await loadGame(state.currentGame.id);
  showToast(t('refresh_ok'), 'info');
}

function resetGameView() {
  state.currentGame = null;
  const boardGrid = document.getElementById('board-grid');
  const placeholder = document.getElementById('board-placeholder');
  const controls = document.getElementById('game-controls');
  const turnIndicator = document.getElementById('turn-indicator');
  const title = document.getElementById('active-game-title');
  const subtitle = document.getElementById('active-game-subtitle');
  const badges = document.getElementById('active-game-badges');

  if (boardGrid) boardGrid.style.display = 'none';
  if (placeholder) placeholder.style.display = 'block';
  if (controls) controls.style.display = 'none';
  if (turnIndicator) turnIndicator.style.display = 'none';
  if (title) title.textContent = 'Aucune partie active';
  if (subtitle) subtitle.textContent = 'Choisissez un jeu à gauche pour commencer.';
  if (badges) badges.innerHTML = '';
}

// ==========================================
// HISTORIQUE DES PARTIES DE L'UTILISATEUR
// ==========================================
async function loadUserGames() {
  const historyList = document.getElementById('games-history-list');
  if (!historyList || !state.token) return;

  try {
    const res = await fetch(`${GAME_API}/games`, {
      headers: getAuthHeaders()
    });

    if (!res.ok) return;

    const games = await res.json();
    historyList.innerHTML = '';

    if (!games || games.length === 0) {
      historyList.innerHTML = `<p style="font-size: 0.85rem; color: var(--text-muted);">${t('no_games')}</p>`;
      return;
    }

    games.reverse().forEach(g => {
      const item = document.createElement('div');
      item.className = 'history-item';
      const shortId = g.id.substring(0, 8);
      const isRunning = g.status === 'ONGOING' || g.status === 'RUNNING';
      const statusIcon = isRunning ? '🟢' : '🏁';

      item.innerHTML = `
        <div>
          <div style="font-weight: 600; font-size: 0.85rem;">${statusIcon} ${g.factoryId || 'Jeu'}</div>
          <div style="font-size: 0.7rem; color: var(--text-muted); font-family: monospace;">#${shortId}...</div>
        </div>
        <button class="btn btn-secondary" style="width: auto; padding: 0.3rem 0.65rem; font-size: 0.75rem;" onclick="loadGame('${g.id}')">
          Ouvrir
        </button>
      `;
      historyList.appendChild(item);
    });
  } catch (err) {
    console.error('Erreur chargement historique :', err);
  }
}

// ==========================================
// PANNEAU D'ADMINISTRATION (ROLE_ADMIN)
// ==========================================
async function loadAdminUsers() {
  const container = document.getElementById('admin-user-list');
  if (!container || !state.token || state.user?.role !== 'ROLE_ADMIN') return;

  try {
    const res = await fetch(`${USER_API}/users`, {
      headers: getAuthHeaders()
    });

    if (!res.ok) return;

    const users = await res.json();
    container.innerHTML = '';

    users.forEach(u => {
      const row = document.createElement('div');
      row.className = 'history-item';
      const isCurrent = state.user?.id === u.id;

      row.innerHTML = `
        <div>
          <div style="font-weight: 600; font-size: 0.85rem;">👤 ${u.username} ${isCurrent ? '(Vous)' : ''}</div>
          <div style="font-size: 0.7rem; color: var(--text-muted);">${u.email || '--'}</div>
        </div>
        <div>
          ${!isCurrent ? `<button class="btn btn-secondary btn-danger" style="width: auto; padding: 0.25rem 0.5rem; font-size: 0.75rem;" onclick="handleDeleteUser('${u.id}')">🗑️</button>` : ''}
        </div>
      `;
      container.appendChild(row);
    });
  } catch (err) {
    console.error('Erreur chargement utilisateurs admin :', err);
  }
}

async function handleDeleteUser(userId) {
  if (!confirm(t('confirm_delete_user'))) return;

  try {
    const res = await fetch(`${USER_API}/users/${userId}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });

    if (!res.ok) {
      showToast('Erreur suppression utilisateur', 'error');
      return;
    }

    showToast(t('user_deleted'), 'success');
    loadAdminUsers();
  } catch (err) {
    showToast('Erreur : ' + err.message, 'error');
  }
}

// ==========================================
// INITIALISATION DE L'APPLICATION
// ==========================================
document.addEventListener('DOMContentLoaded', () => {
  // 1. Initialise la langue
  setLanguage(state.lang);

  // 2. Démarre le moniteur Heartbeat
  startHeartbeatMonitor();

  // 3. Met à jour l'interface d'authentification
  updateAuthUI();

  // 4. Charge l'historique et la liste admin si déjà connecté
  if (state.token && state.user) {
    loadUserGames();
    if (state.user.role === 'ROLE_ADMIN') {
      loadAdminUsers();
    }
  }
});
