Follow 2 
/* 
* “To handle decimals, I treat the decimal as a special, non-movable character. 
A palindrome with a decimal is only valid if the decimal is exactly the middle element. 
If the decimal is at the middle, I mirror the left part normally. 
If the decimal is not at the exact center, the number cannot form a palindrome with a 
decimal, 
so I remove the decimal and compute the next integer palindrome.” 
class Solution { 
   public String nextPalindromeWithDecimal(String s) { 
       // Validate input 
       if (!s.matches("[0-9]*\\.?[0-9]+")) 
           throw new IllegalArgumentException("Invalid numeric input"); 
       int decimalIndex = s.indexOf('.'); 
       // Case 1: No decimal -> normal next palindrome 
       if (decimalIndex == -1) { 
           return nextIntegerPalindrome(s); 
       } 
       // Case 2: Decimal exists -> check if it is exactly at center 
       if (decimalIndex != s.length() / 2) { 
           // Not centered -> decimal must be removed entirely 
           String digits = s.replace(".", ""); 
           return nextIntegerPalindrome(digits); 
       } 
       // Case 3: Decimal IS the center element 
       // L . R structure 
       String L = s.substring(0, decimalIndex); 
       String R = s.substring(decimalIndex + 1); 
       // Mirror check 
       String mirrored = L + "." + new StringBuilder(L).reverse().toString(); 
       if (compare(mirrored, s) > 0) { 
           return mirrored; 
       } 
       // Otherwise increment L and remirror 
       String inc = increment(L); 
       return inc + "." + new StringBuilder(inc).reverse().toString(); 
   } 
   // ==================================================== 
   // SUPPORT FUNCTIONS 
   // ==================================================== 
   private String nextIntegerPalindrome(String s) { 
       char[] arr = s.toCharArray(); 
       int n = arr.length; 
       // Single digit 
       if (n == 1) { 
           if (arr[0] == '9') return "11"; 
           return String.valueOf((char) (arr[0] + 1)); 
       } 
       // All 9s 
       if (allNines(arr)) { 
           StringBuilder sb = new StringBuilder("1"); 
           for (int i = 0; i < n - 1; i++) sb.append("0"); 
           sb.append("1"); 
           return sb.toString(); 
       } 
       char[] mirrored = arr.clone(); 
       mirror(mirrored); 
       if (compareArr(mirrored, arr) > 0) { 
           return new String(mirrored); 
       } 
       char[] inc = arr.clone(); 
       incrementMiddle(inc); 
       mirror(inc); 
       return new String(inc); 
   } 
   private boolean allNines(char[] a) { 
       for (char c : a) if (c != '9') return false; 
       return true; 
   } 
   private int compare(String a, String b) { 
       if (a.length() != b.length()) return Integer.compare(a.length(), b.length()); 
       return a.compareTo(b); 
   } 
   private int compareArr(char[] a, char[] b) { 
       for (int i = 0; i < a.length; i++) { 
           if (a[i] != b[i]) return a[i] - b[i]; 
       } 
       return 0; 
   } 
   private void mirror(char[] a) { 
       int i = 0, j = a.length - 1; 
       while (i < j) a[j--] = a[i++]; 
   } 
   private void incrementMiddle(char[] a) { 
       int n = a.length; 
       int mid = (n - 1) / 2; 
       int i = mid; 
       while (i >= 0 && a[i] == '9') { 
           a[i] = '0'; 
           i--; 
       } 
       a[i]++; 
   } 
   // String increment for left-half when decimal exists 
   private String increment(String L) { 
       char[] arr = L.toCharArray(); 
       int i = arr.length - 1; 
       while (i >= 0 && arr[i] == '9') { 
           arr[i] = '0'; 
           i--; 
       } 
       if (i >= 0) { 
           arr[i]++; 
           return new String(arr); 
       } 
       // overflow like 999 → 1000 
       return "1" + "0".repeat(arr.length); 
   } 
} 
