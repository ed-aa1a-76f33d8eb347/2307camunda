package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import mu.KLogging
import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.ItemEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.ItemRepository
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.PostingEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.controller.PostingRepository


interface PostingService {
    fun getPosting(postingId: Long): Posting
    fun getPostingsByOrderId(orderId: Long): List<Posting>
    fun createPostings(orderId: Long, postings: Collection<CreatePostingRequest>)
    fun updatePosting(request: UpdatePostingRequest): Posting
}


@Service
class PostingServiceImpl(
    private val postingRepository: PostingRepository,
    private val itemRepository: ItemRepository,
) : PostingService {

    override fun getPosting(postingId: Long): Posting {
        val posting = postingRepository.findById(postingId).get()

        return StubPosting(
            postingId = posting.postingId,
            orderId = posting.orderId,
            items = getItemsByPostingId(posting.postingId),
            currency = CurrencyPrice(posting.price, posting.currency),
            postingStatus = posting.postingStatus
        )
    }

    fun getItemsByPostingId(postingId: Long): List<Item> {
        val items = itemRepository.findByPostingIdOrderByItemIdAsc(postingId)
        return items.map { StubItem(itemId = it.itemId, skuId = it.skuId, qty = it.qty, price = it.price) }
    }

    override fun createPostings(orderId: Long, postings: Collection<CreatePostingRequest>) {
        postings.onEach {
            val price = it.items.sumOf { item -> item.price * BigDecimal(item.qty) }
            val posting = postingRepository.save(
                PostingEntity(
                    orderId = orderId,
                    price = price,
                    currency = it.currencyCode
                )
            )
            it.items.onEach { p ->
                itemRepository.save(
                    ItemEntity(
                        skuId = p.skuId,
                        qty = p.qty,
                        price = p.price,
                        postingId = posting.postingId
                    )
                )
            }
        }
    }

    override fun getPostingsByOrderId(orderId: Long): List<Posting> {
        return postingRepository.findByOrderIdOrderByPostingIdAsc(orderId).map { getPosting(it.postingId) }
    }

    override fun updatePosting(request: UpdatePostingRequest): Posting {
        val posting = postingRepository.findById(request.postingId).get()
        request.postingStatus?.let { newStatus ->
            posting.postingStatus = newStatus
        }
        request.currency?.let { newPrice ->
            posting.currency = newPrice.currency
            posting.price = newPrice.price
        }
        return StubPosting(postingRepository.save(posting))
    }


    private companion object : KLogging()
}


