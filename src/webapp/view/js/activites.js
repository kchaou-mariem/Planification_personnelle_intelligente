// Exemple de liaison AJAX avec le backend Java

function chargerActivites() {
    fetch('http://localhost:8083/api/activites') // Adapter l'URL selon votre backend
        .then(response => response.json())
        .then(data => {
            const list = document.getElementById('activites-list');
            list.innerHTML = '';
            data.forEach(act => {
                const div = document.createElement('div');
                div.textContent = act.nom + ' (' + act.date + ')';
                list.appendChild(div);
            });
        })
        .catch(err => {
            alert('Erreur lors du chargement des activités');
            console.error(err);
        });
}
