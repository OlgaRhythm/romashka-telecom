package com.romashka.romashka_telecom.brt.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatabaseTest {
    // Параметры подключения к базе данных
    private static final String URL = "jdbc:postgresql://localhost:5432/brt_db";
    private static final String USER = "brt_user";
    private static final String PASSWORD = "brt_pass";

    public static void main(String[] args) {
        try {
            // Регистрируем драйвер PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Устанавливаем соединение с базой данных
            System.out.println("Подключение к базе данных...");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Подключение успешно установлено!");

            // Создаем Statement для выполнения запросов
            Statement statement = connection.createStatement();

            // Выполняем SELECT запрос
            String sql = "SELECT * FROM calls c JOIN callers cs ON c.caller_id = cs.caller_id WHERE  number = '79001112233' ";
            ResultSet resultSet = statement.executeQuery(sql);
            List<String> subId = new ArrayList<>();

            // Обрабатываем результаты
            System.out.println("\nРезультаты запроса:");
            System.out.println("------------------------");
            while (resultSet.next()) {

                System.out.println("Caller ID: " + resultSet.getLong("caller_id"));
                System.out.println("rate id:" + resultSet.getLong("rate_id"));
                System.out.println("Number: " + resultSet.getString("contact_number"));
                //System.out.println(new String(resultSet.getString("subscriber_name").getBytes("ISO-8859-1"), "UTF-8"));
                // System.out.println(resultSet.getLong("cs.subscriber_id"));
                //System.out.println(resultSet.getLong("subscriber_id"));
                System.out.println(resultSet.getString("start_time"));
                System.out.println(resultSet.getString("end_time"));
                System.out.println("------------------------");
            }
            // Выполняем SELECT запрос
            String sql2 = "SELECT * FROM subscribers WHERE subscriber_id = [subId] ";
            ResultSet resultSet2 = statement.executeQuery(sql);

            // Закрываем ресурсы
            resultSet.close();
            statement.close();
            connection.close();
            System.out.println("Соединение закрыто.");

        } catch (Exception e) {
            System.out.println("Ошибка при работе с базой данных:");
            e.printStackTrace();
        }
    }
} 