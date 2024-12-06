import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class AsyncEchoClient {

    private static final String HOST = "localhost";
    private static final int PORT = 101;

    public static void main(String[] args) {
        new AsyncEchoClient().start();
    }

    public void start() {
        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            // Подключение к серверу
            socketChannel.configureBlocking(true); // Используем блокирующий режим для упрощения
            socketChannel.connect(new InetSocketAddress(HOST, PORT));
            System.out.println("Соединение с сервером установлено.");

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (true) {
                // Ввод сообщения с консоли
                System.out.print("Введите сообщение (или 'exit' для завершения): ");
                String message = scanner.nextLine();

                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println("Завершение работы клиента.");
                    break;
                }

                // Отправка сообщения на сервер
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                // Чтение ответа от сервера
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
                String response = new String(buffer.array(), 0, buffer.limit());
                System.out.println("Ответ от сервера: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
