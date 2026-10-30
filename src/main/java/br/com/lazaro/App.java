package br.com.lazaro;

import br.com.lazaro.domain.*;
import jakarta.persistence.*;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        try (EntityManagerFactory factory = Persistence.createEntityManagerFactory("carrosPU");
             EntityManager em = factory.createEntityManager()) {
            cadastrarExemplos(em);
            em.clear(); // Consulta novamente os dados gravados no banco.
            System.out.println("\n=== TABELAS CRIADAS NO BANCO ===");
            em.createNativeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                    + "WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME")
                    .getResultList().forEach(System.out::println);

            System.out.println("\n=== CARROS E RELACIONAMENTOS ===");
            em.createQuery("select distinct c from Carro c join fetch c.marca "
                    + "left join fetch c.acessorios order by c.id", Carro.class)
                    .getResultList().forEach(c -> System.out.printf("%s | %d | %s | %s%n",
                            c.getModelo(), c.getAno(), c.getMarca().getNome(),
                            c.getAcessorios().stream().map(Acessorio::getNome).sorted()
                                    .collect(Collectors.joining(", "))));

            System.out.println("\n=== CHAVES ESTRANGEIRAS ===");
            em.createNativeQuery("SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS "
                    + "WHERE CONSTRAINT_SCHEMA = 'PUBLIC' AND CONSTRAINT_TYPE = 'FOREIGN KEY' "
                    + "ORDER BY CONSTRAINT_NAME").getResultList().forEach(System.out::println);
            System.out.println("\nVerificacao concluida com sucesso.");
        }
    }

    private static void cadastrarExemplos(EntityManager em) {
        if (em.createQuery("select count(c) from Carro c", Long.class).getSingleResult() > 0) return;
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Marca toyota = new Marca("Toyota");
            Marca honda = new Marca("Honda");
            Acessorio ar = new Acessorio("Ar-condicionado");
            Acessorio camera = new Acessorio("Camera de re");
            Acessorio teto = new Acessorio("Teto solar");
            em.persist(toyota);
            em.persist(honda);
            em.persist(ar);
            em.persist(camera);
            em.persist(teto);
            Carro corolla = new Carro("Corolla", 2025, toyota);
            corolla.adicionarAcessorio(ar);
            corolla.adicionarAcessorio(camera);
            Carro cross = new Carro("Corolla Cross", 2026, toyota);
            cross.adicionarAcessorio(ar);
            cross.adicionarAcessorio(teto);
            Carro civic = new Carro("Civic", 2025, honda);
            civic.adicionarAcessorio(ar);
            civic.adicionarAcessorio(camera);
            civic.adicionarAcessorio(teto);
            em.persist(corolla);
            em.persist(cross);
            em.persist(civic);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}
