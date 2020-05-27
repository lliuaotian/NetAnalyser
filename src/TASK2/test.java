
import java.util.Arrays;

public class test {
  public static void main(String[] args) {
    int a[] = new int[] {1, 2, 3, 4, 5, 6};

    int b[] = Arrays.copyOfRange(a, 1, 3);
    System.out.println(Arrays.toString(b));
  }
}
