import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class AsyncEchoServer {

    private static final int PORT = 101;

    public static void main(String[] args) {
        new AsyncEchoServer().start();
    }

    public void start() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {

            // Настраиваем серверный канал
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false); // Делаем канал неблокирующим
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер запущен на порту " + PORT);

            while (true) {
                selector.select(); // Ожидаем готовности каналов
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove(); // Удаляем обработанный ключ

                    if (key.isAcceptable()) {
                        handleAccept(key, selector);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false); // Делаем канал неблокирующим
        clientChannel.register(selector, SelectionKey.OP_READ); // Регистрируем для чтения
        System.out.println("Новое подключение: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = clientChannel.read(buffer);
        if (bytesRead == -1) {
            // Клиент закрыл соединение
            System.out.println("Клиент отключился: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            return;
        }

        buffer.flip(); // Переключаем в режим чтения
        String message = new String(buffer.array(), 0, buffer.limit());
        System.out.println("Получено сообщение: " + message.trim());

        // Отправляем сообщение обратно клиенту
        buffer.rewind(); // Переключаемся в режим записи для отправки
        clientChannel.write(buffer);

        buffer.clear(); // Очищаем для повторного использования
    }
}
