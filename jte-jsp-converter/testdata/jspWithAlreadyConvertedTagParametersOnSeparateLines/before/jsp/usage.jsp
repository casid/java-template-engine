<%@ taglib prefix="my" uri="/WEB-INF/tld/my.tld" %>
<div>
    <my:jte jte="tag/my/tagWithoutParameters.tag"/>
    <my:jte jte="tag/my/simple.jte" name="${'Hello ' + ' Hola'}" greeting="World"/>
    <span>
        Some text <b><my:jte jte="tag/my/simple.jte"
                             name="${firstName + lastName}"
                             greeting="${greetingText}"
        /></b> and more text <my:jte jte="tag/my/simple.jte"
                                     name="John"
                                     greeting="Smith"
        /> and then no param tag <my:jte jte="tag/my/tagWithoutParameters.tag"/> here
        and even <my:jte jte="tag/my/tagWithoutParameters.tag"
        />here
    </span>
</div>
<span>Just greeting without name: <my:jte jte="tag/my/simple.jte" greeting="Hi"/></span>
<div>
    <my:jte jte="tag/withBody.jte" headline="${title}">
        <p>This is the body, for title ${title}</p>
    </my:jte>
</div>
<div>
    <my:jte jte="tag/withBody.jte" headline="${title}"><p>Body for ${title} without newlines</p></my:jte>
</div>
<span><my:jte jte="tag/withBody.jte" headline="${title}">Simple text</my:jte></span>
