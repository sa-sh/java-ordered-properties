package nu.studer.java.util

import spock.lang.Specification

import static nu.studer.java.util.OrderedProperties.OrderedPropertiesBuilder

class OrderedPropertiesTest extends Specification {

  def props = new OrderedProperties()

  def "empty properties"() {
    setup:
    assert props.isEmpty()
    assert props.size() == 0
  }

  def "get property without default value specified"() {
    setup:
    props.setProperty("aaa", "111")
    assert props.getProperty("aaa") == "111"
    assert props.getProperty("bbb") == null

    props.setProperty("bbb", null)
    assert props.getProperty("bbb") == null
  }

  def "get property with default value specified"() {
    setup:
    props.setProperty("aaa", "111")
    assert props.getProperty("aaa", "222") == "111"
    assert props.getProperty("bbb", "222") == "222"

    props.setProperty("bbb", null)
    assert props.getProperty("bbb", "222") == "222"
  }

  def "properties remain ordered in toString()"() {
    setup:
    props.setProperty("bbb", "222")
    props.setProperty("ccc", "333")
    props.setProperty("aaa", "111")

    assert props.toString() == "{bbb=222, ccc=333, aaa=111}"
  }

  def "properties remain ordered when loading from stream"() {
    setup:
    def stream = asStream """\
b=222
c=333
a=111
"""
    when:
    props.load(stream)

    then:
    props.propertyNames().toList() == ["b", "c", "a"]
    props.stringPropertyNames() == ["b", "c", "a"] as Set
    props.getProperty("b") == "222"
    props.getProperty("c") == "333"
    props.getProperty("a") == "111"
    props.getProperty("d") == null
  }

  def "properties remain ordered when loading from reader"() {
    setup:
    def reader = asReader """\
b=222
c=333
a=111
"""
    when:
    props.load(reader)

    then:
    props.propertyNames().toList() == ["b", "c", "a"]
    props.stringPropertyNames() == ["b", "c", "a"] as Set
    props.getProperty("b") == "222"
    props.getProperty("c") == "333"
    props.getProperty("a") == "111"
    props.getProperty("d") == null
  }

  def "properties remain ordered when loading from stream as xml"() {
    setup:
    def stream = asStream """\
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
  <entry key="b">222</entry>
  <entry key="c">333</entry>
  <entry key="a">111</entry>
</properties>
"""
    when:
    props.loadFromXML(stream)

    then:
    props.propertyNames().toList() == ["b", "c", "a"]
    props.stringPropertyNames() == ["b", "c", "a"] as Set
    props.getProperty("b") == "222"
    props.getProperty("c") == "333"
    props.getProperty("a") == "111"
    props.getProperty("d") == null
  }

  def "properties remain ordered when writing to stream"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, null)

    then:
    stream.toString() endsWith """
b=222
c=333
a=111
"""
  }

  def "properties remain ordered when writing to writer"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def writer = new StringWriter()

    when:
    props.store(writer, null)

    then:
    writer.toString() endsWith """
b=222
c=333
a=111
"""
  }

  def "properties remain ordered when writing to stream as xml"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.storeToXML(stream, "foo")

    then:
    stream.toString() == """\
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>foo</comment>
<entry key="b">222</entry>
<entry key="c">333</entry>
<entry key="a">111</entry>
</properties>
"""
  }

  def "properties remain ordered when writing to stream as xml with custom encoding"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.storeToXML(stream, "foo", "ISO-8859-1")

    then:
    stream.toString() == """\
<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>
<!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
<properties>
<comment>foo</comment>
<entry key="b">222</entry>
<entry key="c">333</entry>
<entry key="a">111</entry>
</properties>
"""
  }

  def "properties can be ordered using custom comparator"() {
    setup:
    props = new OrderedPropertiesBuilder().withOrdering(String.CASE_INSENSITIVE_ORDER).build()
    this.props.setProperty("b", "222")
    this.props.setProperty("c", "333")
    this.props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    this.props.store(stream, null)

    then:
    stream.toString() endsWith """
a=111
b=222
c=333
"""
  }

  def "date can be suppressed when writing to stream without comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, null)

    then:
    stream.toString() == """\
b=222
c=333
a=111
"""
  }

  def "date can be suppressed for empty set of properties when writing to stream without comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, null)

    then:
    stream.toString() == ""
  }

  def "date can be suppressed when writing to stream with comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, "some comment")

    then:
    stream.toString() == """\
#some comment
b=222
c=333
a=111
"""
  }

  def "date can be suppressed for empty set of properties when writing to stream with comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, "some comment")

    then:
    stream.toString() == """\
#some comment
"""
  }

  def "date can be suppressed when writing to stream with long comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, "this is a very long comment that needs to be added when storing the properties to a stream")

    then:
    stream.toString() == """\
#this is a very long comment that needs to be added when storing the properties to a stream
b=222
c=333
a=111
"""
  }

  def "date can be suppressed when writing to stream with multi-line comment"() {
    setup:
    props = new OrderedPropertiesBuilder().suppressDateInComment(true).build()
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")
    def stream = new ByteArrayOutputStream()

    when:
    props.store(stream, "this is some very long comment that spans multiple lines and\nneeds to be added when storing the properties to a stream")

    then:
    stream.toString() == """\
#this is some very long comment that spans multiple lines and
#needs to be added when storing the properties to a stream
b=222
c=333
a=111
"""
  }

  def "properties remain ordered when getting the entrySet"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")

    when:
    Set<Map.Entry<String, String>> entrySet = props.entrySet()

    then:
    assert entrySet.size() == 3
    assert entrySet.collect { def entry -> entry.key } == ["b", "c", "a"]
    assert entrySet.collect { def entry -> entry.value } == ["222", "333", "111"]
  }

  def "can be converted to java.util.Properties"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")

    when:
    Properties jdkProperties = props.toJdkProperties()

    then:
    jdkProperties.size() == 3
    jdkProperties.getProperty("b") == "222"
    jdkProperties.getProperty("c") == "333"
    jdkProperties.getProperty("a") == "111"
  }

  def "properties remain ordered when serializing"() {
    setup:
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")

    when:
    def outStream = new ByteArrayOutputStream()
    new ObjectOutputStream(outStream).writeObject(props)
    OrderedProperties result = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray())).readObject() as OrderedProperties

    then:
    result.size() == 3
    result.propertyNames().toList() == ["b", "c", "a"]
    result.getProperty("b") == "222"
    result.getProperty("c") == "333"
    result.getProperty("a") == "111"
    result.getProperty("d") == null
  }

  def "properties remain ordered when serializing with custom comparator"() {
    setup:
    props = new OrderedPropertiesBuilder().withOrdering(String.CASE_INSENSITIVE_ORDER).build()
    props.setProperty("b", "222")
    props.setProperty("c", "333")
    props.setProperty("a", "111")

    when:
    def outStream = new ByteArrayOutputStream()
    new ObjectOutputStream(outStream).writeObject(props)
    OrderedProperties result = new ObjectInputStream(new ByteArrayInputStream(outStream.toByteArray())).readObject() as OrderedProperties

    then:
    result.size() == 3
    result.propertyNames().toList() == ["a", "b", "c"]
    result.getProperty("a") == "111"
    result.getProperty("b") == "222"
    result.getProperty("c") == "333"
    result.getProperty("d") == null
  }

  def "instances are equal when same properties in same order"() {
    setup:
    props = new OrderedPropertiesBuilder().withOrdering(String.CASE_INSENSITIVE_ORDER).build()
    props.setProperty("c", "333")
    props.setProperty("b", "222")
    props.setProperty("a", "111")

    def otherProps = new OrderedPropertiesBuilder().withOrdering(String.CASE_INSENSITIVE_ORDER).build()
    otherProps.setProperty("a", "111")
    otherProps.setProperty("b", "222")
    otherProps.setProperty("c", "333")

    assert props == otherProps
  }

  def "instances are not equal when same properties in different order"() {
    setup:
    props = new OrderedPropertiesBuilder().withOrdering(String.CASE_INSENSITIVE_ORDER).build()
    props.setProperty("a", "111")
    props.setProperty("b", "222")
    props.setProperty("c", "333")

    def otherProps = new OrderedPropertiesBuilder().withOrdering(Collections.reverseOrder()).build()
    otherProps.setProperty("a", "111")
    otherProps.setProperty("b", "222")
    otherProps.setProperty("c", "333")

    assert props != otherProps
  }

  private static Reader asReader(String text) {
    new StringReader(text)
  }

  private static InputStream asStream(String text) {
    new ByteArrayInputStream(text.getBytes("ISO-8859-1"))
  }

}
