package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;




@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties properties = new Properties();
//        properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
//        properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        //логгирование
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put("hibernate.connection.useUnicode", true);
        properties.put("hibernate.connection.characterEncoding", "UTF-8");
        properties.put("hibernate.connection.charSet", "UTF-8");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "r12xVa_hwe");
//        properties.put(Environment.SHOW_SQL, true);
        //Это позволит не создавать таблицу вручную (или через выполнения sql скрипта).
        properties.put(Environment.HBM2DDL_AUTO, "update");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> players = Collections.emptyList();
        try (Session session = sessionFactory.openSession()) {
//            String sql = "select * from player limit = :limit offset = :offset";
            String sql = "select * from player";
            players = session.createNativeQuery(sql, Player.class)
                    .setMaxResults(pageSize)
                    .setFirstResult( (pageSize * (pageNumber + 1)) - pageSize )
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public int getAllCount() {
        int playersCount = 0;
        try (Session session = sessionFactory.openSession()) {
            playersCount = ((Long) session.createNamedQuery("Player_GetAllCount", Long.class).uniqueResult()).intValue();
            System.out.println(playersCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playersCount;
    }

    @Override
    public Player save(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            player = (Player) session.merge(player);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        Optional<Player> player = Optional.empty();
        try (Session session = sessionFactory.openSession()) {
            player = Optional.of(session.find(Player.class, id));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return player;
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}