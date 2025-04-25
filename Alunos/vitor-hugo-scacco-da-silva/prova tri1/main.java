import java.util.*;

public class Main {
    static class Cliente {
        String nome;
        String email;

        public Cliente(String nome, String email) {
            this.nome = nome;
            this.email = email;
        }
    }

    static abstract class Evento {
        String nome;
        Date dataInicio;
        Date dataFim;
        double valorDiario;
        int capacidade;
        List<Ingresso> ingressos = new ArrayList<>();

        public Evento(String nome, Date dataInicio, Date dataFim, double valorDiario, int capacidade) {
            this.nome = nome;
            this.dataInicio = dataInicio;
            this.dataFim = dataFim;
            this.valorDiario = valorDiario;
            this.capacidade = capacidade;
        }

        public long getDuracaoDias() {
            long diff = dataFim.getTime() - dataInicio.getTime();
            return (diff / (1000 * 60 * 60 * 24)) + 1;
        }

        public boolean temVagasDisponiveis() {
            return ingressos.size() < capacidade;
        }

        public abstract double calcularValor(boolean vip);

        public void adicionarIngresso(Ingresso ingresso) {
            ingressos.add(ingresso);
        }
    }

    static class Show extends Evento {
        public Show(String nome, Date dataInicio, Date dataFim, double valorDiario, int capacidade) {
            super(nome, dataInicio, dataFim, valorDiario, capacidade);
        }

        public int vagasVIP() {
            return (int) (capacidade * 0.1);
        }

        public long ingressosVIPVendidos() {
            return ingressos.stream().filter(Ingresso::isVip).count();
        }

        public boolean temVagaVIP() {
            return ingressosVIPVendidos() < vagasVIP();
        }

        @Override
        public double calcularValor(boolean vip) {
            double base = valorDiario * getDuracaoDias();
            return vip ? base * 1.5 : base;
        }
    }

    static class Congresso extends Evento {
        public Congresso(String nome, Date dataInicio, Date dataFim, double valorDiario, int capacidade) {
            super(nome, dataInicio, dataFim, valorDiario, capacidade);
        }

        @Override
        public double calcularValor(boolean vip) {
            return valorDiario * getDuracaoDias();
        }
    }

    static class Ingresso {
        Cliente cliente;
        Evento evento;
        boolean usado;
        boolean vip;

        public Ingresso(Cliente cliente, Evento evento, boolean vip) {
            this.cliente = cliente;
            this.evento = evento;
            this.vip = vip;
            this.usado = false;
        }

        public void usar() {
            if (!usado) {
                usado = true;
                System.out.println("Ingresso utilizado com sucesso!");
            } else {
                System.out.println("Este ingresso já foi utilizado!");
            }
        }

        public boolean isVip() {
            return vip;
        }
    }

    static Scanner scanner = new Scanner(System.in);
    static List<Cliente> clientes = new ArrayList<>();
    static List<Evento> eventos = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        boolean executando = true;

        while (executando) {
            System.out.println("\n--- Sistema de Gerenciamento de Eventos ---");
            System.out.println("1. Cadastrar cliente");
            System.out.println("2. Cadastrar evento");
            System.out.println("3. Listar eventos");
            System.out.println("4. Comprar ingresso");
            System.out.println("5. Usar ingresso");
            System.out.println("0. Sair");
            System.out.print("Escolha uma opção: ");
            int opcao = Integer.parseInt(scanner.nextLine());

            switch (opcao) {
                case 1:
                    cadastrarCliente();
                    break;
                case 2:
                    cadastrarEvento();
                    break;
                case 3:
                    listarEventos();
                    break;
                case 4:
                    comprarIngresso();
                    break;
                case 5:
                    usarIngresso();
                    break;
                case 0:
                    executando = false;
                    System.out.println("Encerrando o sistema. Até logo, Cleitinho!");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    static void cadastrarCliente() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        clientes.add(new Cliente(nome, email));
        System.out.println("Cliente cadastrado com sucesso!");
    }

    static void cadastrarEvento() throws Exception {
        System.out.print("Nome do evento: ");
        String nome = scanner.nextLine();
        System.out.print("Data início (yyyy-MM-dd): ");
        String[] dataIni = scanner.nextLine().split("-");
        Date inicio = new GregorianCalendar(Integer.parseInt(dataIni[0]),
                Integer.parseInt(dataIni[1]) - 1,
                Integer.parseInt(dataIni[2])).getTime();

        System.out.print("Data fim (yyyy-MM-dd): ");
        String[] dataFim = scanner.nextLine().split("-");
        Date fim = new GregorianCalendar(Integer.parseInt(dataFim[0]),
                Integer.parseInt(dataFim[1]) - 1,
                Integer.parseInt(dataFim[2])).getTime();

        System.out.print("Valor diário: ");
        double valor = Double.parseDouble(scanner.nextLine());
        System.out.print("Capacidade: ");
        int capacidade = Integer.parseInt(scanner.nextLine());
        System.out.print("Tipo (1-Show, 2-Congresso): ");
        int tipo = Integer.parseInt(scanner.nextLine());

        Evento evento;
        if (tipo == 1) evento = new Show(nome, inicio, fim, valor, capacidade);
        else evento = new Congresso(nome, inicio, fim, valor, capacidade);

        eventos.add(evento);
        System.out.println("Evento cadastrado com sucesso!");
    }

    static void listarEventos() {
        if (eventos.isEmpty()) {
            System.out.println("Nenhum evento cadastrado.");
            return;
        }
        for (int i = 0; i < eventos.size(); i++) {
            Evento e = eventos.get(i);
            System.out.println(i + ". " + e.nome + " - " + e.getDuracaoDias() + " dias - Vagas disponíveis: " + (e.capacidade - e.ingressos.size()));
        }
    }

    static void comprarIngresso() {
        listarEventos();
        if (eventos.isEmpty()) return;

        System.out.print("Escolha o número do evento: ");
        int idx = Integer.parseInt(scanner.nextLine());
        if (idx < 0 || idx >= eventos.size()) {
            System.out.println("Evento inválido.");
            return;
        }
        Evento evento = eventos.get(idx);
        if (!evento.temVagasDisponiveis()) {
            System.out.println("Evento lotado!");
            return;
        }

        System.out.print("Nome do cliente: ");
        String nome = scanner.nextLine();
        Cliente cliente = clientes.stream().filter(c -> c.nome.equalsIgnoreCase(nome)).findFirst().orElse(null);
        if (cliente == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }

        boolean vip = false;
        if (evento instanceof Show show) {
            System.out.print("Deseja área VIP? (s/n): ");
            vip = scanner.nextLine().equalsIgnoreCase("s") && show.temVagaVIP();
            if (!vip && !show.temVagaVIP()) System.out.println("Sem vagas VIP disponíveis.");
        }

        Ingresso ingresso = new Ingresso(cliente, evento, vip);
        evento.adicionarIngresso(ingresso);
        System.out.printf("Ingresso comprado por R$%.2f\n", evento.calcularValor(vip));
    }

    static void usarIngresso() {
        System.out.print("Nome do cliente: ");
        String nome = scanner.nextLine();
        Cliente cliente = clientes.stream().filter(c -> c.nome.equalsIgnoreCase(nome)).findFirst().orElse(null);
        if (cliente == null) {
            System.out.println("Cliente não encontrado.");
            return;
        }
        for (Evento e : eventos) {
            for (Ingresso i : e.ingressos) {
                if (i.cliente == cliente && !i.usado) {
                    i.usar();
                    return;
                }
            }
        }
        System.out.println("Nenhum ingresso válido encontrado.");
    }
}
