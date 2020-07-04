# Rediblaster

Rediblaster is a small tool built for publishing mock data into a local Redis instance for other services, tests, and experimentation. It utilizes [Jedis](https://github.com/xetorthio/jedis) as the java Redis client.

The POJO being inserted is an Employee object.

```Java
public class Employee {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String occupation;
    private final int salary;

    ...
}
```

Rediblaster will create ids from 0 to n, where n is the number of employees to be generated (this is defined as a static var in Main.java). For each id, an employee object with random values for each field will be created, converted to a json string via a Jackson ObjectMapper, and pushed to Redis. the key/value will be id:Employee.

The fields are generated as such:
* firstName: 5 character string of random letters (lowercase)
* lastName: 8 character string of random letters (lowercase)
* email: {firstName}.{lastName}@email.com
* occupation: one of 4 strings: Full Stack Developer, Frontend Developer, Backend Developer, Manager
* salary: random integer between 50,000 and 250,000

```json
Key: "124"
Value: "{\"id\":124,\"firstName\":\"oyhwj\",\"lastName\":\"eimuzbzw\",\"email\":\"oyhwj.eimuzbzw@email.com\",\"occupation\":\"Backend Developer\",\"salary\":213004}"
```


In order to use Rediblaster, you must first have Redis setup locally. Download the official released package [here](http://download.redis.io/releases/redis-6.0.5.tar.gz), or clone the [source code](https://github.com/redis-io/redis) from github and run MAKE. Definitely recommend the latter option as Redis is extremely well written IMO. Doesn't hurt to review some C.
