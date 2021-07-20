@import static example.JteContext.*

<div>
    @tag.my.tagWithoutParameters()
    @tag.my.simple(
        name = "Hello " + " Hola",
        greeting = "World"
    )
    <span>
        Some text <b>@tag.my.simple(
                         name = firstName + lastName,
                         greeting = greetingText
                     )</b> and more text @tag.my.simple(
                                             name = "John",
                                             greeting = "Smith"
                                         ) and then no param tag @tag.my.tagWithoutParameters() here
        and even @tag.my.tagWithoutParameters()here
    </span>
</div>
<span>Just greeting without name: @tag.my.simple(
                                      greeting = "Hi"
                                  )</span>
<div>
    @tag.withBody(
        headline = title,
        bodyContent = @`
        <p>This is the body, for title ${title}</p>
    `
    )
</div>
<div>
    @tag.withBody(
        headline = title,
        bodyContent = @`<p>Body for ${title} without newlines</p>`
    )
</div>
<span>@tag.withBody(
          headline = title,
          bodyContent = @`Simple text`
      )</span>
