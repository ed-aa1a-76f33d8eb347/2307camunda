package ru.spb.ivsamokhvalov.example.demo.camunda.service

import java.math.BigDecimal
import mu.KLogging
import org.springframework.stereotype.Service
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.ItemEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.ItemRepository
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingEntity
import ru.spb.ivsamokhvalov.example.demo.camunda.repo.PostingRepository


interface PostingService {
    fun getPosting(postingId: Long): Posting
    fun getPostingsByOrderId(orderId: Long): List<Posting>
    fun createPostings(orderId: Long, postings: Collection<CreatePostingRequest>)
    fun updatePosting(request: UpdatePostingRequest): Posting
}


@Service
class PostingServiceImpl(
    private val postingRepository: PostingRepository,
    private val itemService: PostingItemService,
) : PostingService {

    override fun getPosting(postingId: Long): Posting {
        val posting = postingRepository.findById(postingId).get()

        return PostingDto(
            postingId = posting.postingId,
            orderId = posting.orderId,
            items = itemService.getItemByPostingId(posting.postingId),
            currency = CurrencyPrice(posting.price, posting.currency),
            postingStatus = posting.postingStatus
        )
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
            itemService.createPostingItems(posting.postingId, it.currencyCode, it.items)
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
        return PostingDto(postingRepository.save(posting))
    }


    private companion object : KLogging()
}


