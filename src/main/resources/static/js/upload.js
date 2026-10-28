document.querySelectorAll('[data-upload]').forEach(function (bloco) {
    var entrada = bloco.querySelector('input[type=file]');
    var previa = bloco.querySelector('.previa');
    var marcarPrimeira = bloco.dataset.upload === 'novo';

    entrada.addEventListener('change', function () {
        previa.innerHTML = '';
        Array.from(entrada.files).forEach(function (arquivo, indice) {
            var item = document.createElement('label');
            item.className = 'previa-item';

            var imagem = document.createElement('img');
            imagem.src = URL.createObjectURL(arquivo);
            imagem.alt = arquivo.name;

            var opcao = document.createElement('span');
            var radio = document.createElement('input');
            radio.type = 'radio';
            radio.name = 'indicePrincipal';
            radio.value = indice;
            radio.checked = marcarPrimeira && indice === 0;
            opcao.appendChild(radio);
            opcao.appendChild(document.createTextNode(' Principal'));

            item.appendChild(imagem);
            item.appendChild(opcao);
            previa.appendChild(item);
        });
    });
});
