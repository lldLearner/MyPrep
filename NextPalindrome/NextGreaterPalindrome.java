Q)NextSmallestPalindrome 
public class NextPalindrome { 
 public static String nextPalindrome(String s) { 
       // Validate input: must be positive integer with no leading zeros 
       if (!s.matches("[1-9]\\d*")) { 
           throw new IllegalArgumentException("Input must be a positive integer with no 
leading zeros"); 
       } 
       int n = s.length(); 
       char[] arr = s.toCharArray(); 
       // ------------------------- 
       // Case 1: Single digit 
       // ------------------------- 
       if (n == 1) { 
           if (s.equals("9")) 
               return "11";       // next palindrome greater than 9 
           return String.valueOf((char)(arr[0] + 1));  // e.g. 3→4, 8→9 
       } 
       // ------------------------- 
       // Case 2: All 9s -> 9, 99, 999 → 11, 101, 1001, ... 
       // ------------------------- 
       if (allNines(arr)) { 
           StringBuilder sb = new StringBuilder("1"); 
           for (int i = 0; i < n - 1; i++) sb.append('0'); 
           sb.append("1"); 
           return sb.toString(); 
       } 
       // ------------------------- 
       // Step 1: Try simple mirroring 
       // ------------------------- 
       char[] mirrored = arr.clone(); 
       mirror(mirrored); 
       if (isGreater(mirrored, arr)) { 
           return new String(mirrored); 
       } 
       // ------------------------- 
       // Step 2: Increment middle AND then mirror 
       // ------------------------- 
       char[] inc = arr.clone(); 
       incrementMiddle(inc); 
       mirror(inc); 
       return new String(inc); 
   } 
   // ------------------------------------------- 
   // Helper Functions 
   // ------------------------------------------- 
   private static boolean allNines(char[] a) { 
       for (char c : a) if (c != '9') return false; 
       return true; 
   } 
   // Compare two char arrays representing numbers 
   private static boolean isGreater(char[] a, char[] b) { 
       for (int i = 0; i < a.length; i++) { 
           if (a[i] > b[i]) return true; 
           if (a[i] < b[i]) return false; 
       } 
       return false;  // equal or smaller 
   } 
   // Mirror left half onto right half 
   private static void mirror(char[] a) { 
       int i = 0, j = a.length - 1; 
       while (i < j) { 
           a[j] = a[i]; 
           i++; j--; 
       } 
   } 
   // Increment middle digit(s) and propagate carry 
   private static void incrementMiddle(char[] a) { 
       int n = a.length; 
       int mid = (n - 1) / 2; 
       int i = mid; 
       // Carry chain 
       while (i >= 0 && a[i] == '9') { 
           a[i] = '0'; 
           i--; 
       } 
       a[i]++;  // Add carry 
   } 
 public static void main(String[] args) { 
  String[] inputs = { 
       "9999", 
       "12321", 
       "12945", 
       "1999993", 
       "123456789", 
       "129999", 
       "10", 
       "8", 
       "1001", 
       "99999" 
   }; 
  for (String t : inputs) { 
   System.out.println(t + " → " + nextPalindrome(t)); 
  } 
 } 
} 
/* 
* 
✅
 TIME COMPLEXITY — O(n) (Optimal) 
Let n = number of digits in the input string. 
The algorithm does at most these operations: 
1⃣ Check if all digits are '9' 
for (char c : arr) 
Runs once → O(n) 
2⃣ Mirror left half onto right half 
while (i < j) { a[j] = a[i]; } 
Moves pointers inward → O(n/2) = O(n) 
3⃣ Comparison: is mirrored > original? 
for (i = 0 .. n-1) 
→ O(n) 
4⃣ Increment middle + propagate carry 
Worst-case: 
999000 → 000000  (carries through entire left half) 
But only propagates through half the number → O(n/2) = O(n) 
5⃣ Mirror again after increment 
→ O(n) 
�
�
 Total Time Complexity: 
O(n) + O(n) + O(n) + O(n) + O(n) = O(n) 
This is the best possible complexity, because you must inspect at least half the digits to 
create a palindrome. 
�
�
 Final Time Complexity = O(n) 
�
�
 SPACE COMPLEXITY — O(n) 
Where does extra memory come from? 
1⃣ Copying arrays 
We use: 
char[] mirrored = arr.clone(); 
char[] inc = arr.clone(); 
Each clone requires O(n) space. 
2⃣ Output String 
The returned palindrome also takes O(n) space. 
�
�
 Total Space Complexity: 
Input array:     O(n) 
Clone arrays:    O(n) 
Output string:   O(n) -------------------------------- 
Total            O(n) 
There is no recursion and no stack growth, so no additional overhead. 
�
�
 Final Space Complexity = O(n) 
* 
*/ 
