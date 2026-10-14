# Biblioteca geral: Spotify e YouTube Music

Implementação da issue #1, atualizada em 14/10/2026 após o pedido de uma
biblioteca geral, com no máximo um ícone indicando a origem de cada item.
Esta versão substitui os atalhos externos anteriores.

## Comportamento

- Configurações → Conta permite conectar Spotify e Google independentemente
  da fonte escolhida para Início e Busca.
- Biblioteca reúne playlists, álbuns salvos, artistas seguidos e músicas
  salvas/curtidas das duas contas. Os itens compartilham os mesmos filtros,
  busca, ordenação e visualização em grade ou lista.
- Um ícone pequeno identifica a origem do item, com descrição para leitor
  de tela. Não há grupos separados por serviço nem prefixos nos títulos.
- Abrir uma playlist ou tocar uma música escolhe o serviço pela URI do item,
  sem exigir troca manual de fonte. Os menus usam essa mesma origem.
- O filtro Músicas contém músicas salvas/curtidas; ele não percorre todas as
  playlists para copiar suas faixas. Itens de mesmo nome em serviços diferentes
  permanecem distintos. Repetições da mesma URI aparecem uma vez por coleção.
- Uma falha em um serviço preserva os itens carregados do outro. A tela indica
  carregamento e falha parcial, com opção de tentar novamente.
- Sair ou trocar uma conta descarta seus itens e respostas antigas, preservando
  a outra conta. A renovação normal de token não invalida a coleção.

## Reprodução e limites

A biblioteca é conjunta; a fila de reprodução continua sendo de uma origem
por vez. Ao tocar uma música da outra origem, começa a fila desse serviço.
Adicionar à fila só é oferecido no menu para a origem atual; os gestos de fila
avisam quando é necessário tocar a música para trocar o serviço. Não há
conversão de faixas entre serviços, transferência de playlists, assinatura
conjunta nem crossfade entre provedores.

As integrações existentes do Square foram reutilizadas, sem servidor ou
biblioteca de terceiros adicional. Como explicado no
[README](../README.md#disclaimer), são integrações não oficiais e dependem de
protocolos privados. Conectar uma conta Google não verifica assinatura
Premium nem garante equivalência com os benefícios dos aplicativos oficiais.

## Abordagem técnica

`LibraryContents` agrega as quatro coleções e elimina duplicações por URI.
`MainViewModel` consulta os dois backends em paralelo, conserva resultados
parciais da mesma sessão e invalida resultados quando a revisão da conta muda.
Spotify percorre páginas da API para não limitar a biblioteca à primeira página;
YouTube reutiliza a playlist de músicas curtidas da conta.

A URI já distingue Spotify, YouTube e arquivos locais. `backendFor(uri)` e
`backendIdOf(uri)` reaproveitam essa identidade no catálogo, nos menus e no
player. A preferência de descoberta não determina mais a origem desses itens.
O motor Spotify pode permanecer pausado para consultar o catálogo enquanto
YouTube toca. As trocas de player são serializadas; desligamento e saída da
conta também encerram o motor estacionado.

## Revisão com cinco especialistas

| Especialidade | Pontos revisados e correções incorporadas |
|---|---|
| Android | Liberação do motor estacionado, cancelamento de conexão e troca de conta |
| Reprodução | Persistência da fila YouTube, Spotify remoto e uma reprodução local ativa |
| Segurança | Isolamento de contas, invalidação do cache e rejeição de respostas antigas |
| Experiência | Um ícone por item, contas acessíveis, menus pela origem e estados offline |
| Qualidade | Mesclagem por URI, paginação e cenários de falha parcial e troca de origem |

As revisões são estáticas, não testes com contas reais.

## Como validar em Android

1. Conectar Spotify e Google em Configurações → Conta. Abrir Biblioteca e
   confirmar playlists, álbuns, artistas e músicas das duas contas juntos.
2. Usar os filtros, procurar título/artista e alternar grade/lista. Conferir
   músicas de mesmo título em serviços diferentes e um único ícone de origem.
3. Abrir e tocar uma playlist de cada origem sem mudar a fonte em Configurações.
   Inverter a origem que toca e a origem do item cujo menu está aberto: download,
   edição, artista e adicionar à playlist devem usar o item selecionado.
4. Tentar adicionar à fila uma música de outra origem e confirmar a orientação;
   tocar nela deve trocar o player. Testar fila vazia, pausa e retomada.
5. Impedir acesso a um dos serviços, atualizar a biblioteca e confirmar que o
   outro permanece. Restabelecer acesso e usar Tentar novamente.
6. Sair apenas do Spotify, conferir que os itens Google permanecem e entrar com
   outra conta Spotify. Repetir para Google, inclusive durante o carregamento.
7. Reiniciar durante reprodução YouTube, alternar serviços rapidamente e sair
   do Spotify durante conexão. Conferir fila, posição, silêncio do motor
   estacionado e controle Spotify remoto.
8. Testar arquivos locais, downloads sem rede, fonte ampliada e TalkBack.

## Verificação automatizada e pendências

Há quatro testes de mesclagem e identidade em `LibraryContentsTest`: itens
homônimos de serviços distintos, duplicação nas quatro coleções, origem vazia
sem apagar a outra e identificação de URIs Spotify/YouTube/local.

A compilação Kotlin e os testes unitários usam o ferramental Android do projeto.
Neste ambiente, o SDK 37 é distribuído como `android-37.0`, exigindo um script
Gradle temporário para selecionar esse alvo; a tarefa `cargoBuild` foi excluída.
Isso não valida o motor Rust nem gera um APK instalável.

A matriz Android acima permanece pendente: não há aparelho/emulador nem contas
reais disponíveis nesta execução. A validação visual, TalkBack, áudio real e
concorrência do motor nativo precisam ser feitas no dispositivo.
