// JS pour la page de connexion

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const data = {
        email: document.getElementById('login-email').value,
        motdepasse: document.getElementById('login-password').value
    };
    fetch('http://localhost:8083/api/utilisateurs/login', {
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
