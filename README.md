# workflow-monitor

## O que

Esse eh um projeto Gradle a parte focado em desenvolver um CLI de monitoramente dos workflows do CI, processos consumindo memoria pelo tempo,
geracao de CSV com possibilidade de gerar grafico de consumo, etc.

## Motivo

A necessidade surgiu pois nao temos visibilidade do que esta rodando e o quanto esta consumindo durante um Job do Github Actions.

Por exemplo: Deu Out of memory, quais processos estavam rodando? quantos gigas eles estavam consumindo? Essas respostas nos nunca temos quando falha um workflow.

Inspirado um pouco no projeto: https://github.com/almahmoud/workflow-telemetry-action/

Motivo por nao usar o projeto acima: Ele nao gera um grafico de consumo por processo e ele usa uma API de terceiro para gerar alguns graficos
o que pode ser considerado um problema de seguranca.

## Objetivos

- Coletar top processos de consumo de memoria, comando inicializador, pid, repetidamente ate completar o workflow todo.
- Exportar logs Raw
- Exportar CSV que seja possivel gerar uma grafico do consumo pelo periodo de tempo.

## Como

A ideia inicia eh criar um CLI que podemos chamar alguns comandos:
- Start -> Inicia a coleta e salva em um arquivo RAW constamente
- Stop -> Para o servidor rodando
- Csv -> Gera o arquivo Raw e gera um CSV
- Status -> Valida se o CLI esta rodando (necessario ?)

Para atingir esse comandos precisamos que rodar um servidor em background na maquina que fique durante o Job coletando e no final do job
precisar ser finalizado, chamando o Stop.

Passo a passo do funcionamento:

Start:

- Valida se tem um servidor ja aberto, se tiver, nao faz nada
- Inicia um servidor Web em background que ira ficar coletando as informacoes, o servidor web sera usado para executar comandos
  de Csv e Stop

Stop:
- Faz uma chamada Http para o Servidor na maquina para seu processo

Csv:
- Pega o arquivo Raw, le e gera um CSV em cima.