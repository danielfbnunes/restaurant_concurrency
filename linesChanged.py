def main():
     RED = '\033[91m'
     END = '\033[0m'
     file = open("RestaurantLog.txt", "r")
     lines = file.readlines()
     i = 0
     while i <= len(lines)-2:
         x = lines[i].split(' ')
         y = lines[i+1].split(' ')
         z = ""
         #print(x)
         if len(y) < 9 or len(x) < 9 :
            if i == 0 or i == 1:
                z += lines[i]
            else:
                if len(y) < 9:
                    z += lines[i+1]
                if len(x) < 9:
                    z += lines[i+1]
         else:
             if i == 2:
                 z+= lines[i]+lines[i+1]
             else:
                t = 0
                while t < len(x) - 1:
                    if x[t] != y[t]:
                        z+= RED + y[t] + RED + " "
                    else:
                        z+= END + y[t] + END + " "

                    t = t + 1

         i = i + 1

         print(z.strip())


main()
