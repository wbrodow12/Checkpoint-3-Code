import random

setOfStudentIds = set()
levels = ['FR','SO', 'JR', 'SR']

# Generate a random set of 1000 unique student IDs sampled from the range 
# [10000000,1000000000] inclusive. (Using a "set" guarantees uniqueness)
while len(setOfStudentIds) < 1000:
    setOfStudentIds.add(random.randint(10000000,1000000000))

# Generate recordIDs by starting with 0 and creating the next ID by 
# incrementing the previous ID by one. 
# Then, write the student records to 'Student.csv'
with open('Student.csv', 'w') as f:
    recordID = 0
    for studentId in setOfStudentIds:
        level = levels[random.randint(0,3)]
        age = random.randint(17,22)
        major = 'CS'
        name = 'Test Student'
        
        studentRecord = f'{studentId},{name},{major},{level},{age},{recordID}\n'
        f.write(studentRecord)
        recordID = recordID + 1

# Create the test input file associated with the 'Student.csv' created above
with open('input.txt', 'w') as f:
    f.write('3\n') # Use degree of 3
    f.write('print\n') # Print the initial state of the tree after loading 'Student.csv'

    # For each student ID in 'Student.csv', 
    #   search the B+ Tree for the Student ID (The record should exist)
    #   delete the record associated with the student ID (This delete should succeed)
    #   search the B+ Tree for the Student ID (The record should NOT exist)
    for studentId in setOfStudentIds:
        f.write(f'search {studentId}\n')
        f.write(f'delete {studentId}\n')
        f.write(f'search {studentId}\n')

    f.write('print\n') # Print the state of the tree after deleting all records
    
    # For each student ID, 
    #   search the B+ Tree for the Student ID (The record should NOT exist)
    #   insert a record associated with the student ID (This insert should succeed)
    #   search the B+ Tree for the Student ID (The record should exist)
    for studentId in setOfStudentIds:
        level = levels[random.randint(0,3)]
        age = random.randint(17,22)
        major = 'CS'
        name = 'Test Student'
        f.write(f'search {studentId}\n')
        f.write(f'insert {studentId} {name} {major} {level} {age}\n')
        f.write(f'search {studentId}\n')

    f.write('print\n') # Print the state of the tree after inserting 1000 records

exit()