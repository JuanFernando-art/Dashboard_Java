/**
 * ARQUIVO: logica.js
 * FUNÇÃO: Controlar a interface do usuário (UI) e a comunicação com o servidor Java.
 * EXPLICAÇÃO PARA O FRONT-END: Este arquivo faz o "meio de campo". Ele busca os dados
 * no Java (via fetch), transforma o JSON em linhas de tabela e envia novos dados
 * via formulários.
 */

// --- 📦 GERENCIAMENTO DE PRODUTOS ---

/**
 * Busca a lista de produtos no Back-end e desenha a tabela na tela.
 * Inclui a lógica de cores para controle de estoque crítico.
 */
async function carregarProdutos() {
    try {
        // Pede os produtos ao Java (AppRest)
        const resposta = await fetch('http://localhost:7000/api/produtos');
        const produtos = await resposta.json();

        const corpoTabela = document.getElementById('tabela-corpo');
        corpoTabela.innerHTML = ""; // Limpa a tabela antes de carregar

        produtos.forEach(p => {
            // LÓGICA DE DESTAQUE: Calcula a saúde do estoque
            const porcentagem = (p.quantidade / p.quantidadeInicial) * 100;
            let classeDestaque = "";

            if (porcentagem <= 20) {
                classeDestaque = "estoque-critico"; // Geralmente vermelho no CSS
            } else if (porcentagem <= 50) {
                classeDestaque = "estoque-alerta";  // Geralmente amarelo no CSS
            }

            // Cria a linha da tabela com Template String
            const linha = `
                <tr class="${classeDestaque}"> 
                    <td>${p.id}</td>
                    <td>${p.nome}</td>
                    <td>${p.categoria}</td>
                    <td>${p.quantidade} / ${p.quantidadeInicial}</td>
                    <td>R$ ${p.precoVenda.toFixed(2)}</td>
                    <td>
                        <button onclick='prepararEdicao(${JSON.stringify(p)})'>Editar</button>
                        <button onclick='deletarProduto(${p.id})'>Excluir</button>
                    </td>
                </tr>
            `;
            corpoTabela.innerHTML += linha;
        });

    } catch (erro) {
        console.error("Erro ao carregar dados do Java:", erro);
    }
}

// Inicia a listagem assim que o script é carregado
carregarProdutos();

/**
 * Prepara o formulário para edição preenchendo os campos com os dados do produto.
 */
function prepararEdicao(p) {
    abrirModal();
    document.getElementById('modalTitulo').innerText = "Editar Produto";
    document.getElementById('prodId').value = p.id;
    document.getElementById('nome').value = p.nome;
    document.getElementById('categoria').value = p.categoria;
    document.getElementById('precoCusto').value = p.precoCusto;
    document.getElementById('precoVenda').value = p.precoVenda;
    document.getElementById('quantidade').value = p.quantidade;
    document.getElementById('quantidadeInicial').value = p.quantidadeInicial;
    validarLimite(); // Verifica se a quantidade não ultrapassa o estoque inicial
}

/**
 * Envia os dados do formulário para o Java (POST para novo, PUT para editar).
 */
document.getElementById('formProduto').addEventListener('submit', async (e) => {
    e.preventDefault(); // Impede a página de recarregar

    const id = document.getElementById('prodId').value;

    // Monta o objeto exatamente como o Model 'Produto.java' espera
    const produto = {
        nome: document.getElementById('nome').value,
        idCategoria: parseInt(document.getElementById('categoria').value),
        idEmpreendimento: 1,
        precoCusto: parseFloat(document.getElementById('precoCusto').value),
        precoVenda: parseFloat(document.getElementById('precoVenda').value),
        quantidade: parseInt(document.getElementById('quantidade').value),
        quantidadeInicial: parseInt(document.getElementById('quantidadeInicial').value)
    };

    if (id) produto.id = parseInt(id);

    // Envia para o Back-end
    await fetch('http://localhost:7000/api/produtos', {
        method: id ? 'PUT' : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(produto)
    });

    fecharModal();
    carregarProdutos(); // Recarrega a tabela atualizada
});

// --- 💰 SISTEMA DE PDV (VENDAS) ---

let carrinho = [];

/**
 * Processa a finalização da venda:
 * 1. Salva o registro da venda (Histórico).
 * 2. Atualiza o estoque de cada item (Baixa automática).
 */
async function finalizarVenda() {
    if (carrinho.length === 0) return alert("O carrinho está vazio!");

    // Calcula total e gera texto descritivo para o banco
    const totalVenda = carrinho.reduce((acc, item) => acc + (item.precoVenda * item.quantidadeCarrinho), 0);
    const listaNomes = carrinho.map(item => `${item.nome} (${item.quantidadeCarrinho})`).join(", ");

    try {
        // 1. Envia a Venda para o histórico (VendaDAO)
        await fetch('http://localhost:7000/api/vendas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ total: totalVenda, produtosVendidos: listaNomes })
        });

        // 2. Dá baixa no estoque de cada item vendido (ProdutoDAO.subtrairEstoque)
        for (const item of carrinho) {
            await fetch('http://localhost:7000/api/produtos/subtrair', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: item.id,
                    quantidadeVendida: item.quantidadeCarrinho,
                    idEmpreendimento: 1
                })
            });
        }

        alert("Venda realizada com sucesso!");
        carrinho = [];
        document.getElementById('modalPDV').style.display = 'none';

        // Atualiza a visualização para refletir o novo estoque
        await carregarProdutos();
        mostrarProdutos();

    } catch (error) {
        console.error("Erro na venda:", error);
        alert("Houve um erro ao processar a venda.");
    }
}

// --- 🔍 UTILITÁRIOS (FILTROS E MODAIS) ---

/**
 * Filtra a tabela de produtos por nome ou por status (Crítico/Alerta).
 */
function filtrarTabela() {
    const termo = document.getElementById('inputBusca').value.toLowerCase();
    const filtroStatus = document.getElementById('filtroEstoque').value;
    const linhas = document.querySelectorAll('#tabela-corpo tr');

    linhas.forEach(linha => {
        const texto = linha.innerText.toLowerCase();
        const possuiTermo = texto.includes(termo);

        const isCritico = linha.classList.contains('estoque-critico');
        const isAlerta = linha.classList.contains('estoque-alerta');

        let atendeFiltro = true;
        if (filtroStatus === 'critico') atendeFiltro = isCritico;
        else if (filtroStatus === 'alerta') atendeFiltro = isAlerta;

        // Esconde ou mostra a linha
        linha.style.display = (possuiTermo && atendeFiltro) ? "" : "none";
    });
}

/**
 * Impede que o usuário cadastre mais produtos do que o limite inicial permitido.
 */
function validarLimite() {
    const qtd = parseInt(document.getElementById('quantidade').value) || 0;
    const limite = parseInt(document.getElementById('quantidadeInicial').value) || 0;
    const btn = document.querySelector('.btn-save');
    const erro = document.getElementById('erroLimite');

    if (qtd > limite) {
        btn.disabled = true;
        btn.style.opacity = "0.5";
        erro.style.display = "block";
    } else {
        btn.disabled = false;
        btn.style.opacity = "1";
        erro.style.display = "none";
    }
}