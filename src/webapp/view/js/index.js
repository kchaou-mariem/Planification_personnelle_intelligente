// JS pour la page d'accueil avec gestion des formulaires
console.log('Page d\'accueil chargée');

document.getElementById('registerForm').addEventListener('submit', function(e) {
	e.preventDefault();
	const data = {
		nom: document.getElementById('reg-nom').value,
		prenom: document.getElementById('reg-prenom').value,
		email: document.getElementById('reg-email').value,
		age: document.getElementById('reg-age').value,
		genre: document.getElementById('reg-genre').value,
		poste: document.getElementById('reg-poste').value,
		motdepasse: document.getElementById('reg-password').value
	};
	fetch('http://localhost:8085/api/utilisateurs/register', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(data)
	})
	.then(res => res.json())
	.then(res => {
		document.getElementById('form-message').style.color = '#27ae60';
		document.getElementById('form-message').textContent = 'Inscription réussie !';
		document.getElementById('registerForm').reset();
	})
	.catch(err => {
		document.getElementById('form-message').style.color = '#e74c3c';
		document.getElementById('form-message').textContent = 'Erreur lors de l\'inscription';
		console.error(err);
	});
});

document.getElementById('loginForm').addEventListener('submit', function(e) {
	e.preventDefault();
	const data = {
		email: document.getElementById('login-email').value,
		motdepasse: document.getElementById('login-password').value
	};
	fetch('http://localhost:8085/api/utilisateurs/login', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(data)
	})
	.then(res => res.json())
	.then(res => {
		if(res.succes) {
			document.getElementById('form-message').style.color = '#27ae60';
			document.getElementById('form-message').textContent = 'Connexion réussie !';
			// Rediriger ou stocker le token si besoin
		} else {
			document.getElementById('form-message').style.color = '#e74c3c';
			document.getElementById('form-message').textContent = 'Email ou mot de passe incorrect';
		}
	})
	.catch(err => {
		document.getElementById('form-message').style.color = '#e74c3c';
		document.getElementById('form-message').textContent = 'Erreur lors de la connexion';
		console.error(err);
	});
});