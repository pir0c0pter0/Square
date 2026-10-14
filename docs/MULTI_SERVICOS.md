# Multi serviços: Spotify e YouTube Premium

Plano da issue #1, elaborado em 14/10/2026 com cinco especialistas: produto e
experiência, arquitetura Android, Spotify, YouTube e segurança/validação.
Esta entrega é de planejamento; não altera o funcionamento do aplicativo.

## Objetivo e interpretação

Permitir que uma pessoa que usa Spotify e YouTube Premium tenha um ponto de
acesso aos dois serviços. Interpreta-se “junto” como acesso no mesmo aplicativo,
com escolha explícita do serviço, não como reprodução simultânea ou assinatura
única. Cada assinatura, conta, cobrança e benefício continua no seu provedor.

O YouTube Premium inclui YouTube Music Premium, mas isso não garante os mesmos
benefícios dentro de clientes de terceiros. A referência para usar esses
benefícios são os aplicativos e sites oficiais, conforme a
[ajuda do YouTube Premium](https://support.google.com/youtube/answer/6308116?hl=pt-BR).

## Decisão recomendada

Começar com uma área de atalhos que abre Spotify e YouTube Music nos aplicativos
oficiais, ou nos respectivos sites. Não conectar novas contas, consultar
catálogos, receber credenciais nem controlar reprodução nessa primeira etapa.
Essa é uma proposta conservadora de navegação, não uma declaração de aprovação
pelos provedores. As integrações privadas existentes no Square não são prova de
que um novo produto integrado seria autorizado.

A [política do Spotify](https://developer.spotify.com/policy), seções III.5 e
III.7, restringe integração com conteúdo de outros serviços e mistura de áudio.
Sua seção IV.1 exige Premium para streaming musical pela plataforma. Assim,
manter duas contas e um único áudio ativo não basta para considerar um player
integrado permitido. A exceção para transferência de dados pessoais/metadados
de playlists em III.9 também não autoriza reprodução conjunta.

As [políticas das APIs do YouTube](https://developers.google.com/youtube/terms/developer-policies)
restringem download sem autorização, separação de áudio/vídeo e reprodução em
segundo plano por clientes dessas APIs. A
[YouTube Data API](https://developers.google.com/youtube/v3/getting-started)
oferece operações sobre recursos como vídeos e playlists; não deve ser tratada
como uma API de áudio do YouTube Music Premium. Não foi identificado nas fontes
consultadas um mecanismo público que transfira todos os benefícios Premium
para o player nativo do Square.

## Diagnóstico do Square atual

Os caminhos abaixo são relativos a `app/src/main/java/dev/lelonio/square/`,
exceto quando indicado. O diagnóstico foi feito com CodeGraph e a documentação
do repositório; não representa teste com contas reais.

| Componente | Comportamento existente | Consequência para o plano |
|---|---|---|
| `backend/MusicBackend.kt` | `BackendId` distingue Spotify e YouTube Music; cada backend reconhece suas URIs | Reutilizar essa identidade se uma integração interna for autorizada |
| `SquareApplication.kt` | Mantém instâncias e credenciais dos dois serviços; `activeBackend` segue a preferência | As contas já têm armazenamento separado; não criar outro gerenciador |
| `data/PreferencesStore.kt` | Guarda uma fonte selecionada | Hoje a experiência alterna Spotify ou YouTube Music |
| `ui/settings/SettingsScreen.kt` | Escolhe fonte; seção Google só aparece com YouTube ativo | Coexistência interna exigiria tornar as duas contas acessíveis |
| `playback/PlaybackService.kt` | Troca reprodutor; encerra motor Spotify ao entrar no YouTube | Não existe roteamento geral de fila mista por origem |
| `data/PlaybackStore.kt` | Guarda uma fila na chave `state` | Tocar em outro serviço pode substituir a fila anterior |
| `auth/SpotifyOAuth.kt`, `auth/WebApiAccount.kt` | OAuth com PKCE e sessões para finalidades distintas | Não confundir login do motor privado com autorização da API oficial |
| `backend/youtube/YouTubeAccount.kt` | Sessão Google por cookies e estado do canal | Login não comprova assinatura Premium |
| `data/Gateway.kt` | Catálogo específico do Spotify | Não transformá-lo em agregador dos serviços |
| `app/src/main/AndroidManifest.xml` | Square recebe links Spotify | Um atalho externo precisa evitar abrir o próprio Square novamente |

O [README](../README.md#disclaimer) já informa que o projeto usa protocolo Spotify
reimplementado e endpoints privados do YouTube Music. O plano não apresenta
esses caminhos como integrações oficiais nem propõe ampliá-los para oferecer
benefícios Premium.

## Pareceres dos cinco especialistas

| Especialidade | Recomendação incorporada |
|---|---|
| 1. Produto e experiência | Escolha clara do serviço; não prometer assinatura conjunta, catálogo mesclado ou versão equivalente de uma faixa |
| 2. Arquitetura Android | Reaproveitar navegação e componentes existentes; não mexer nos motores para construir atalhos |
| 3. Spotify | Usar aplicativo oficial inicialmente; condicionar integração interna ao aceite do caso de uso pelo provedor |
| 4. YouTube Premium | Distinguir conta conectada de assinatura; benefícios ficam no ambiente oficial, sem deduzir Premium de cookie |
| 5. Segurança e validação | Não coletar segredos no hub; validar destino dos links, ausência de aplicativo e isolamento de sessões em etapa futura |

## Experiência da primeira versão proposta

1. Em Configurações, abrir uma seção “Serviços”. Mostrar “Abrir Spotify” e
   “Abrir YouTube Music”, com explicação curta de que cada conta e assinatura
   é gerenciada no serviço escolhido.
2. Ao tocar, pausar a reprodução local do Square antes de sair, sem apagar sua
   fila. Não iniciar áudio automaticamente no destino nem comandar o outro app.
3. Abrir o aplicativo oficial com destino explícito. Se não estiver disponível,
   oferecer abertura do site oficial em navegador. Se nenhum destino puder
   abrir, informar o erro e permanecer no Square.
4. Ao voltar, manter a reprodução do Square pausada; a pessoa decide retomá-la.
   Não inferir estado, conta ou assinatura do aplicativo externo.

Destinos iniciais fixos: `https://open.spotify.com/` e
`https://music.youtube.com/`. Não aceitar links arbitrários nessa versão.
Evitar um `ACTION_VIEW` implícito que retorne ao Square: usar o pacote oficial
quando disponível e um navegador para o fallback. Tratar falha de abertura
mesmo após verificar disponibilidade. Seguir os
[intents comuns do Android](https://developer.android.com/guide/components/intents-common)
e a [visibilidade de pacotes](https://developer.android.com/training/package-visibility).

## Etapas e condições de avanço

| Etapa | Entrega | Condição de saída |
|---|---|---|
| 0 — Planejamento (esta issue) | Diagnóstico, cinco pareceres, decisão, escopo e validação | Documento revisável e versionado, sem nova funcionalidade de reprodução |
| 1 — Atalhos (issue futura) | Seção Serviços e abertura externa, sem APIs ou novas contas | Critérios A1–A5 abaixo verificados em Android |
| 2 — Viabilidade de integração | Confirmar por escrito com os provedores o caso de uso, permissões, distribuição e benefícios possíveis | Evidência de autorização e protótipo com APIs oficiais; se não viável, manter apenas os atalhos |
| 3 — Integração interna, somente se autorizada | Duas contas independentes, busca/biblioteca por fonte e uma reprodução ativa | Critérios B1–B6, migração e testes com contas autorizadas |

Não se atribuem prazos à autorização externa. A implementação dos atalhos deve
usar Kotlin/Compose e intents já disponíveis, sem servidor, banco novo ou
dependência adicional. Os candidatos são `ui/settings/SettingsScreen.kt`,
recursos de texto, `app/src/main/AndroidManifest.xml` e o ponto existente de pausa no player;
os arquivos efetivos serão definidos ao implementar a etapa 1.

Se a etapa 3 for autorizada, reutilizar `MusicBackend`, `BackendId`, PKCE e
armazenamentos independentes. O
[fluxo PKCE do Spotify](https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow)
é apropriado para clientes sem segredo embutido. Usar identificação própria,
permissões mínimas e respeitar as
[quotas vigentes](https://developer.spotify.com/documentation/web-api/concepts/quota-modes).
Não garantir aprovação de expansão de usuários nem contornar limites criando
aplicativos adicionais.

Filas separadas exigiriam slots por serviço e por arquivos locais em
`PlaybackStore`, com migração da chave antiga pela origem das URIs. Salvar pela
origem do reprodutor efetivo, pois a preferência já pode ter mudado; serializar
trocas em `PlaybackService`; restaurar somente uma fila inteiramente compatível
e sempre pausada. O legado ambíguo deve ser preservado até resolver sua origem,
sem descartar dados silenciosamente. Não duplicar um campo de provedor quando
a URI já identifica a origem.

Ficam fora do escopo: fila mista, sobreposição/crossfade entre serviços,
sincronização bidirecional, substituição automática de músicas, downloads
Premium por terceiros, cobrança conjunta e armazenamento central de cookies.

## Critérios de aceitação para implementação futura

| ID | Cenário | Resultado esperado |
|---|---|---|
| A1 | Abrir cada atalho com os apps oficiais instalados | Abre o serviço escolhido; não retorna ao Square nem inicia áudio por comando do hub |
| A2 | App ausente; navegador disponível | Oferece site oficial e abre somente após a ação da pessoa |
| A3 | App/navegador indisponível ou abertura falha | Erro claro; Square continua utilizável e fila permanece intacta |
| A4 | Square tocando antes do atalho; voltar ao Square | Áudio local pausa antes de sair e continua pausado ao voltar |
| A5 | Revisar textos e persistência do hub | Não solicita credenciais nem mostra Premium como verificado; controles acessíveis por leitor de tela |
| B1 | Conectar duas contas e reiniciar o app | Sessões independentes persistem; fonte atual continua identificada |
| B2 | Alternar serviço e voltar | Uma reprodução ativa; cada fila e posição restaurada pausada, sem perder a outra |
| B3 | Expirar/revogar/desconectar uma conta | Mensagem identifica o serviço; não remove a sessão do outro; apaga os dados pessoais aplicáveis à conta desconectada |
| B4 | Migrar fila legada, vazia, local ou incompatível | Sem crash, perda silenciosa ou envio de URI ao reprodutor incorreto |
| B5 | API sem autorização, indisponível ou limitada | Erro identificável; sem loop de login, repetição ilimitada ou troca automática de serviço |
| B6 | Revisar logs, relatórios, armazenamento e consentimento | Sem tokens/cookies/senhas em logs ou URLs; permissões mínimas; operações de escrita explicitamente confirmadas |

A1–A5 são a matriz mínima da etapa 1. Executar em Android 8 e Android 11+,
incluindo leitor de tela, apps ausentes, falha de abertura e links Spotify com
Square definido como receptor. B1–B6 só se aplicam à etapa autorizada; testar
também troca rápida, encerramento do processo, acesso offline e conta sem
benefício Premium. Esses testes são propostos, não executados nesta issue.

## Riscos e perguntas ainda abertas

- Os provedores autorizariam o caso de uso e a distribuição pretendida? Sem
  essa evidência, não iniciar reprodução integrada.
- Quais benefícios YouTube Premium podem efetivamente ser oferecidos por um
  cliente autorizado? Não prometer equivalência com o aplicativo oficial.
- APIs, políticas, quotas e protocolos privados mudam. Revalidar fontes antes
  de cada etapa; os links refletem a consulta de 14/10/2026.
- Abertura externa depende da configuração Android. O hub não consegue
  garantir que outro aplicativo estará autenticado ou que só haverá um áudio
  ativo fora do Square.

O próximo trabalho concreto recomendado é a etapa 1, em issue separada. Esta
issue entrega o plano solicitado e não habilita nenhuma das etapas futuras.
